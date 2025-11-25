package com.example.styloandroid.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {
    private val repo = BookingRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _bookingStatus = MutableLiveData<String?>()
    val bookingStatus: LiveData<String?> = _bookingStatus

    // Carrega os serviços quando entra na tela do estabelecimento
    fun loadServices(providerId: String) {
        viewModelScope.launch {
            _services.value = repo.getServicesForProvider(providerId)
        }
    }

    fun scheduleService(providerId: String, businessName: String, service: Service, timestamp: Long) {
        viewModelScope.launch {
            val appointment = Appointment(
                providerId = providerId,
                businessName = businessName,
                clientName = repo.getCurrentUserName() ?: "Cliente",
                serviceId = service.id,
                serviceName = service.name,
                price = service.price,
                date = timestamp,
                status = "pending"
            )

            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "Agendamento realizado com sucesso!"
            } else {
                _bookingStatus.value = "Erro ao agendar. Tente novamente."
            }
        }
    }
}