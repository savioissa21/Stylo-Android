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

class BookingViewModel : ViewModel() {
    private val repo = BookingRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _team = MutableLiveData<List<AppUser>>()
    val team: LiveData<List<AppUser>> = _team

    private val _bookingStatus = MutableLiveData<String?>()
    val bookingStatus: LiveData<String?> = _bookingStatus

    fun loadServices(providerId: String) {
        viewModelScope.launch {
            _services.value = repo.getServicesForProvider(providerId)
        }
    }

    // Carrega quem trabalha no local
    fun loadTeam(providerId: String) {
        viewModelScope.launch {
            _team.value = repo.getTeamForEstablishment(providerId)
        }
    }

    fun scheduleService(
        providerId: String,
        businessName: String,
        service: Service,
        employee: AppUser, // Agora recebemos QUEM vai fazer
        timestamp: Long
    ) {
        viewModelScope.launch {
            _bookingStatus.value = null

            // Verifica disponibilidade DO FUNCIONÁRIO
            val isTaken = repo.isTimeSlotTaken(employee.uid, timestamp, service.durationMin)
            
            if (isTaken) {
                _bookingStatus.value = "⚠️ Horário indisponível para ${employee.name}!"
                return@launch
            }

            val appointment = Appointment(
                providerId = providerId, // Dono do negócio
                businessName = businessName,
                
                employeeId = employee.uid, // Quem executa
                employeeName = employee.name,
                
                clientName = repo.getCurrentUserName() ?: "Cliente",
                serviceId = service.id,
                serviceName = service.name,
                price = service.price,
                durationMin = service.durationMin,
                date = timestamp,
                status = "pending"
            )

            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "Agendamento com ${employee.name} confirmado! 🎉"
            } else {
                _bookingStatus.value = "Erro ao agendar. Tente novamente."
            }
        }
    }
}