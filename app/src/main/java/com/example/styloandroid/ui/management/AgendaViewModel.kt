package com.example.styloandroid.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import kotlinx.coroutines.launch

class AgendaViewModel : ViewModel() {

    private val repo = BookingRepository()

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    fun loadAppointments() {
        viewModelScope.launch {
            _appointments.value = repo.getProviderAppointments()
        }
    }

    fun updateStatus(appointmentId: String, newStatus: String) {
        viewModelScope.launch {
            val success = repo.updateAppointmentStatus(appointmentId, newStatus)
            if (success) {
                _statusMsg.value = "Status atualizado para $newStatus"
                loadAppointments() // Recarrega a lista para mostrar a mudança
            } else {
                _statusMsg.value = "Erro ao atualizar status"
            }
        }
    }
}