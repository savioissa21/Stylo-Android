package com.example.styloandroid.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingViewModel : ViewModel() {
    private val repo = BookingRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _team = MutableLiveData<List<AppUser>>()
    val team: LiveData<List<AppUser>> = _team

    private val _bookingStatus = MutableLiveData<String?>()
    val bookingStatus: LiveData<String?> = _bookingStatus

    private val _ratingStats = MutableLiveData<Pair<Double, Int>>()
    val ratingStats: LiveData<Pair<Double, Int>> = _ratingStats

    private val _availableSlots = MutableLiveData<List<Long>>()
    val availableSlots: LiveData<List<Long>> = _availableSlots

    private val _isLoadingSlots = MutableLiveData<Boolean>()
    val isLoadingSlots: LiveData<Boolean> = _isLoadingSlots

    // Armazena info do estabelecimento (horários, dias)
    private var providerInfo: AppUser? = null

    fun loadServices(providerId: String) {
        viewModelScope.launch {
            // Carrega info do provedor junto com serviços
            providerInfo = repo.getProviderInfo(providerId)
            _services.value = repo.getServicesForProvider(providerId)
        }
    }

    fun loadTeam(providerId: String) {
        viewModelScope.launch { _team.value = repo.getTeamForEstablishment(providerId) }
    }

    fun loadReviews(providerId: String) {
        viewModelScope.launch { _ratingStats.value = repo.getReviewsStats(providerId) }
    }

    // Helper para verificar se o dia está aberto
    fun isEstablishmentOpenOn(date: Calendar): Boolean {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        // Se providerInfo ainda não carregou, assume padrão (Seg-Sab)
        val workDays = providerInfo?.workDays ?: listOf(2,3,4,5,6,7)
        return workDays.contains(dayOfWeek)
    }

    /**
     * GERAÇÃO DE HORÁRIOS DINÂMICA
     */
    fun loadTimeSlots(date: Calendar, durationMin: Int, employeeId: String) {
        _isLoadingSlots.value = true
        viewModelScope.launch {
            // 1. Configura Horário de Funcionamento Dinâmico
            var startHour = 9
            var startMin = 0
            var endHour = 20
            var endMin = 0

            providerInfo?.let { info ->
                // Parse "09:00" -> hour=9, min=0
                info.openTime?.split(":")?.let {
                    if(it.size == 2) {
                        startHour = it[0].toIntOrNull() ?: 9
                        startMin = it[1].toIntOrNull() ?: 0
                    }
                }
                info.closeTime?.split(":")?.let {
                    if(it.size == 2) {
                        endHour = it[0].toIntOrNull() ?: 20
                        endMin = it[1].toIntOrNull() ?: 0
                    }
                }
            }

            // 2. Define o intervalo do dia para busca no banco
            val startOfDay = date.clone() as Calendar
            startOfDay.set(Calendar.HOUR_OF_DAY, 0)
            startOfDay.set(Calendar.MINUTE, 0)

            val endOfDay = date.clone() as Calendar
            endOfDay.set(Calendar.HOUR_OF_DAY, 23)
            endOfDay.set(Calendar.MINUTE, 59)

            // 3. Busca agendamentos existentes
            val existingAppointments = repo.getAppointmentsForEmployeeOnDate(employeeId, startOfDay.timeInMillis, endOfDay.timeInMillis)

            // 4. Gera os Slots
            val slots = mutableListOf<Long>()
            val slotCalendar = date.clone() as Calendar

            // Configura horário inicial dinâmico
            slotCalendar.set(Calendar.HOUR_OF_DAY, startHour)
            slotCalendar.set(Calendar.MINUTE, startMin)
            slotCalendar.set(Calendar.SECOND, 0)
            slotCalendar.set(Calendar.MILLISECOND, 0)

            val now = System.currentTimeMillis()

            // Converte horário de fechamento para minutos totais do dia para facilitar comparação
            val closeTimeInMinutes = (endHour * 60) + endMin

            // Loop de geração
            while (true) {
                val currentHour = slotCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMin = slotCalendar.get(Calendar.MINUTE)
                val currentTimeInMinutes = (currentHour * 60) + currentMin

                // Se o início do slot já passou do horário de fechar, para.
                if (currentTimeInMinutes >= closeTimeInMinutes) break

                val slotStart = slotCalendar.timeInMillis
                val slotEnd = slotStart + (durationMin * 60 * 1000)

                // Verifica se o TÉRMINO do serviço passa do horário de fechar
                val endSlotCal = Calendar.getInstance().apply { timeInMillis = slotEnd }
                val endSlotTimeInMinutes = (endSlotCal.get(Calendar.HOUR_OF_DAY) * 60) + endSlotCal.get(Calendar.MINUTE)

                // Se terminar depois do fechamento (e for no mesmo dia), ignora
                if (endSlotCal.get(Calendar.DAY_OF_YEAR) == slotCalendar.get(Calendar.DAY_OF_YEAR) &&
                    endSlotTimeInMinutes > closeTimeInMinutes) {
                    break
                }

                // Verifica se já passou (para hoje) + buffer 30min
                if (slotStart < now + (30 * 60 * 1000)) {
                    slotCalendar.add(Calendar.MINUTE, 30)
                    continue
                }

                // Verifica colisão
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

                // Avança 30 min
                slotCalendar.add(Calendar.MINUTE, 30)
            }

            _availableSlots.value = slots
            _isLoadingSlots.value = false
        }
    }

    fun createAppointment(appointment: Appointment) {
        _bookingStatus.value = null
        viewModelScope.launch {
            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "✅ Agendamento realizado com sucesso!"
            } else {
                _bookingStatus.value = "❌ Erro ao agendar. Tente novamente."
            }
        }
    }
}