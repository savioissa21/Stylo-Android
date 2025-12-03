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

    private val _bookingStatus = MutableLiveData<String?>()
    val bookingStatus: LiveData<String?> = _bookingStatus

    private val _ratingStats = MutableLiveData<Pair<Double, Int>>()
    val ratingStats: LiveData<Pair<Double, Int>> = _ratingStats

    private val _availableSlots = MutableLiveData<List<Long>>()
    val availableSlots: LiveData<List<Long>> = _availableSlots

    private val _isLoadingSlots = MutableLiveData<Boolean>()
    val isLoadingSlots: LiveData<Boolean> = _isLoadingSlots

    // Armazena info global do estabelecimento (para fallback)
    private var establishmentInfo: AppUser? = null

    fun loadServices(providerId: String) {
        viewModelScope.launch {
            // Carrega info do estabelecimento
            establishmentInfo = repo.getProviderInfo(providerId)
            _services.value = repo.getServicesForProvider(providerId)
        }
    }

    fun loadTeam(providerId: String) {
        viewModelScope.launch { _team.value = repo.getTeamForEstablishment(providerId) }
    }

    fun loadReviews(providerId: String) {
        viewModelScope.launch { _ratingStats.value = repo.getReviewsStats(providerId) }
    }

    // Helper para verificar se o dia está aberto (apenas dia da semana)
    // A verificação de data bloqueada específica acontece dentro do loadTimeSlots agora
    fun isEstablishmentOpenOn(date: Calendar): Boolean {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        val workDays = establishmentInfo?.workDays ?: listOf(2,3,4,5,6,7)
        return workDays.contains(dayOfWeek)
    }

    /**
     * GERAÇÃO DE HORÁRIOS AVANÇADA
     * Considera: Horário individual, Almoço, Folgas e Agendamentos existentes.
     */
    fun loadTimeSlots(date: Calendar, durationMin: Int, employeeId: String) {
        _isLoadingSlots.value = true
        viewModelScope.launch {

            // 1. Busca configurações ESPECÍFICAS do profissional (seja funcionário ou dono)
            // Se falhar, usa as configs globais do establishmentInfo
            val employeeConfig = repo.getEmployeeConfig(employeeId) ?: establishmentInfo

            if (employeeConfig == null) {
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            // 2. VERIFICA BLOQUEIOS DE DATA (Feriados/Folgas)
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = sdfDate.format(date.time)

            if (employeeConfig.blockedDates?.contains(dateString) == true) {
                // Dia bloqueado para este funcionário
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            // 3. VERIFICA DIA DA SEMANA (Recurso existente, mas aplicado ao funcionário)
            val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
            val workDays = employeeConfig.workDays ?: listOf(2,3,4,5,6,7)
            if (!workDays.contains(dayOfWeek)) {
                _availableSlots.value = emptyList()
                _isLoadingSlots.value = false
                return@launch
            }

            // 4. CONFIGURA HORÁRIOS (Parse HH:mm para Minutos do dia)
            val (startHour, startMin) = parseTime(employeeConfig.openTime ?: "09:00")
            val (endHour, endMin) = parseTime(employeeConfig.closeTime ?: "20:00")

            val startWorkMinutes = startHour * 60 + startMin
            val endWorkMinutes = endHour * 60 + endMin

            // Configura Almoço (se existir)
            var startLunchMinutes = -1
            var endLunchMinutes = -1
            if (!employeeConfig.lunchStartTime.isNullOrEmpty() && !employeeConfig.lunchEndTime.isNullOrEmpty()) {
                val (lh, lm) = parseTime(employeeConfig.lunchStartTime)
                startLunchMinutes = lh * 60 + lm
                val (leh, lem) = parseTime(employeeConfig.lunchEndTime)
                endLunchMinutes = leh * 60 + lem
            }

            // 5. DEFINE INTERVALO DE BUSCA NO BANCO
            val startOfDay = date.clone() as Calendar
            startOfDay.set(Calendar.HOUR_OF_DAY, 0)
            startOfDay.set(Calendar.MINUTE, 0)
            startOfDay.set(Calendar.SECOND, 0)

            val endOfDay = date.clone() as Calendar
            endOfDay.set(Calendar.HOUR_OF_DAY, 23)
            endOfDay.set(Calendar.MINUTE, 59)
            endOfDay.set(Calendar.SECOND, 59)

            // Busca agendamentos existentes no banco
            val existingAppointments = repo.getAppointmentsForEmployeeOnDate(employeeId, startOfDay.timeInMillis, endOfDay.timeInMillis)

            // 6. GERAÇÃO DOS SLOTS
            val slots = mutableListOf<Long>()
            val slotCalendar = date.clone() as Calendar

            // Seta o calendário para o início do expediente
            slotCalendar.set(Calendar.HOUR_OF_DAY, startHour)
            slotCalendar.set(Calendar.MINUTE, startMin)
            slotCalendar.set(Calendar.SECOND, 0)
            slotCalendar.set(Calendar.MILLISECOND, 0)

            val now = System.currentTimeMillis()

            // Loop minuto a minuto (saltando de 30 em 30 min, ou conforme duração)
            // Aqui usamos um "step" fixo de 30min para grade de horários, ou a própria duração do serviço?
            // Geralmente grades são fixas (ex: 9:00, 9:30, 10:00) independente se o serviço dura 20 ou 50 min.
            // Vamos manter grade de 30 em 30 minutos para padronização.
            val slotStepMinutes = 30

            while (true) {
                val currentHour = slotCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMin = slotCalendar.get(Calendar.MINUTE)
                val currentMinutesOfDay = currentHour * 60 + currentMin

                // Se o slot começar DEPOIS ou IGUAL ao horário de saída, encerra.
                // Obs: Consideramos que o serviço precisa caber ANTES de fechar.
                if (currentMinutesOfDay >= endWorkMinutes) break

                val slotStart = slotCalendar.timeInMillis
                val slotEnd = slotStart + (durationMin * 60 * 1000)

                // Verifica término do serviço
                val endSlotCal = Calendar.getInstance().apply { timeInMillis = slotEnd }
                val endSlotMinutesOfDay = endSlotCal.get(Calendar.HOUR_OF_DAY) * 60 + endSlotCal.get(
                    Calendar.MINUTE)

                // Se virar o dia ou passar do horário de saída
                if (endSlotCal.get(Calendar.DAY_OF_YEAR) != slotCalendar.get(Calendar.DAY_OF_YEAR) ||
                    endSlotMinutesOfDay > endWorkMinutes) {
                    break // Não cabe mais hoje
                }

                // VALIDAÇÃO 1: Passado (com buffer de 30min)
                if (slotStart < now + (30 * 60 * 1000)) {
                    slotCalendar.add(Calendar.MINUTE, slotStepMinutes)
                    continue
                }

                // VALIDAÇÃO 2: Almoço
                // Verifica se o slot INTERCEPTA o horário de almoço
                // Lógica de interseção: (SlotInicio < AlmoçoFim) E (SlotFim > AlmoçoInicio)
                if (startLunchMinutes != -1 && endLunchMinutes != -1) {
                    if (currentMinutesOfDay < endLunchMinutes && endSlotMinutesOfDay > startLunchMinutes) {
                        // Conflito com almoço
                        slotCalendar.add(Calendar.MINUTE, slotStepMinutes)
                        continue
                    }
                }

                // VALIDAÇÃO 3: Conflito com Agendamentos Existentes
                var isTaken = false
                for (appointment in existingAppointments) {
                    val appStart = appointment.date
                    val appEnd = appStart + (appointment.durationMin * 60 * 1000)

                    // Lógica de interseção de tempo
                    if (slotStart < appEnd && slotEnd > appStart) {
                        isTaken = true
                        break
                    }
                }

                if (!isTaken) {
                    slots.add(slotStart)
                }

                // Avança para o próximo slot
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
        _bookingStatus.value = null
        viewModelScope.launch {
            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "✅ Agendamento realizado com sucesso!"
            } else {
                _bookingStatus.value = "❌ Erro ao agendar. Tente novamente."
            }
        }
    }

    fun getEmployeesForService(service: Service): List<AppUser> {
        val allEmployees = _team.value ?: emptyList()

        return allEmployees.filter { emp ->
            // Regra: Se a lista do serviço estiver vazia, todos fazem (ou só o dono).
            // Se tiver IDs, filtra quem está na lista (pelo UID ou Email, conforme sua lógica de salvamento)
            service.employeeIds.isEmpty() ||
                    service.employeeIds.contains(emp.uid) ||
                    service.employeeIds.contains(emp.email)
        }
    }
}