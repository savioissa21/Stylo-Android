// Caminho: app/src/main/java/com/example/styloandroid/ui/client/booking/BookingViewModel.kt

package com.example.styloandroid.ui.client.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.data.repository.BookingRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingViewModel : ViewModel() {
    private val repo = BookingRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _team = MutableLiveData<List<AppUser>>()
    val team: LiveData<List<AppUser>> = _team

    // --- NOVO: LiveData para expor os dados do Estabelecimento (Banner, Endereço, etc) ---
    private val _establishment = MutableLiveData<AppUser?>()
    val establishment: LiveData<AppUser?> = _establishment

    private val _bookingStatus = MutableLiveData<String?>()
    val bookingStatus: LiveData<String?> = _bookingStatus

    private val _ratingStats = MutableLiveData<Pair<Double, Int>>()
    val ratingStats: LiveData<Pair<Double, Int>> = _ratingStats

    private val _availableSlots = MutableLiveData<List<Long>>()
    val availableSlots: LiveData<List<Long>> = _availableSlots

    private val _isLoadingSlots = MutableLiveData<Boolean>()
    val isLoadingSlots: LiveData<Boolean> = _isLoadingSlots

    private val _isBookingLoading = MutableLiveData<Boolean>()
    val isBookingLoading: LiveData<Boolean> = _isBookingLoading

    // Mantemos uma referência privada para validações de horário internas
    private var establishmentInfo: AppUser? = null

    fun loadServices(providerId: String) {
        viewModelScope.launch {
            // Carrega info do provedor
            val info = repo.getProviderInfo(providerId)
            establishmentInfo = info

            // --- ATUALIZAÇÃO: Publica os dados para a View observar ---
            _establishment.value = info

            // Carrega serviços
            _services.value = repo.getServicesForProvider(providerId)
        }
    }

    fun loadTeam(providerId: String) {
        viewModelScope.launch { _team.value = repo.getTeamForEstablishment(providerId) }
    }

    fun loadReviews(providerId: String) {
        viewModelScope.launch { _ratingStats.value = repo.getReviewsStats(providerId) }
    }

    fun isEstablishmentOpenOn(date: Calendar): Boolean {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        val workDays = establishmentInfo?.workDays ?: listOf(2,3,4,5,6,7)
        return workDays.contains(dayOfWeek)
    }

    fun loadTimeSlots(date: Calendar, durationMin: Int, employeeId: String) {
        _isLoadingSlots.value = true
        viewModelScope.launch {
            val employeeConfig = repo.getEmployeeConfig(employeeId) ?: establishmentInfo

            if (employeeConfig == null) {
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = sdfDate.format(date.time)

            if (employeeConfig.blockedDates?.contains(dateString) == true) {
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
            val workDays = employeeConfig.workDays ?: listOf(2,3,4,5,6,7)
            if (!workDays.contains(dayOfWeek)) {
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            val (startHour, startMin) = parseTime(employeeConfig.openTime ?: "09:00")
            val (endHour, endMin) = parseTime(employeeConfig.closeTime ?: "20:00")

            val startWorkMinutes = startHour * 60 + startMin
            val endWorkMinutes = endHour * 60 + endMin

            var startLunchMinutes = -1
            var endLunchMinutes = -1
            if (!employeeConfig.lunchStartTime.isNullOrEmpty() && !employeeConfig.lunchEndTime.isNullOrEmpty()) {
                val (lh, lm) = parseTime(employeeConfig.lunchStartTime)
                startLunchMinutes = lh * 60 + lm
                val (leh, lem) = parseTime(employeeConfig.lunchEndTime)
                endLunchMinutes = leh * 60 + lem
            }

            val startOfDay = date.clone() as Calendar
            startOfDay.set(Calendar.HOUR_OF_DAY, 0); startOfDay.set(Calendar.MINUTE, 0); startOfDay.set(Calendar.SECOND, 0)
            val endOfDay = date.clone() as Calendar
            endOfDay.set(Calendar.HOUR_OF_DAY, 23); endOfDay.set(Calendar.MINUTE, 59); endOfDay.set(Calendar.SECOND, 59)

            val existingAppointments = repo.getAppointmentsForEmployeeOnDate(employeeId, startOfDay.timeInMillis, endOfDay.timeInMillis)

            val slots = mutableListOf<Long>()
            val slotCalendar = date.clone() as Calendar
            slotCalendar.set(Calendar.HOUR_OF_DAY, startHour)
            slotCalendar.set(Calendar.MINUTE, startMin)
            slotCalendar.set(Calendar.SECOND, 0)
            slotCalendar.set(Calendar.MILLISECOND, 0)

            val now = System.currentTimeMillis()
            val slotStepMinutes = 30

            while (true) {
                val currentHour = slotCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMin = slotCalendar.get(Calendar.MINUTE)
                val currentMinutesOfDay = currentHour * 60 + currentMin

                if (currentMinutesOfDay >= endWorkMinutes) break

                val slotStart = slotCalendar.timeInMillis
                val slotEnd = slotStart + (durationMin * 60 * 1000)
                val endSlotCal = Calendar.getInstance().apply { timeInMillis = slotEnd }
                val endSlotMinutesOfDay = endSlotCal.get(Calendar.HOUR_OF_DAY) * 60 + endSlotCal.get(Calendar.MINUTE)

                if (endSlotCal.get(Calendar.DAY_OF_YEAR) != slotCalendar.get(Calendar.DAY_OF_YEAR) ||
                    endSlotMinutesOfDay > endWorkMinutes) {
                    break
                }

                if (slotStart < now + (30 * 60 * 1000)) {
                    slotCalendar.add(Calendar.MINUTE, slotStepMinutes)
                    continue
                }

                if (startLunchMinutes != -1 && endLunchMinutes != -1) {
                    if (currentMinutesOfDay < endLunchMinutes && endSlotMinutesOfDay > startLunchMinutes) {
                        slotCalendar.add(Calendar.MINUTE, slotStepMinutes)
                        continue
                    }
                }

                var isTaken = false
                for (appointment in existingAppointments) {
                    val appStart = appointment.date
                    val appEnd = appStart + (appointment.durationMin * 60 * 1000)
                    if (slotStart < appEnd && slotEnd > appStart) {
                        isTaken = true
                        break
                    }
                }

                if (!isTaken) {
                    slots.add(slotStart)
                }
                slotCalendar.add(Calendar.MINUTE, slotStepMinutes)
            }

            _availableSlots.value = slots
            _isLoadingSlots.value = false
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Pair(h, m)
    }

    fun createAppointment(appointment: Appointment) {
        _isBookingLoading.value = true
        _bookingStatus.value = null
        viewModelScope.launch {
            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "✅ Agendamento realizado com sucesso!"
            } else {
                _bookingStatus.value = "❌ Erro ao agendar. Tente novamente."
            }
            _isBookingLoading.value = false
        }
    }

    fun getEmployeesForService(service: Service): List<AppUser> {
        val allEmployees = _team.value ?: emptyList()
        return allEmployees.filter { emp ->
            service.employeeIds.isEmpty() ||
                    service.employeeIds.contains(emp.uid) ||
                    service.employeeIds.contains(emp.email)
        }
    }
}