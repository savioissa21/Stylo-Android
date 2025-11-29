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

    private val _ratingStats = MutableLiveData<Pair<Double, Int>>()
    val ratingStats: LiveData<Pair<Double, Int>> = _ratingStats

    fun loadServices(providerId: String) {
        viewModelScope.launch { _services.value = repo.getServicesForProvider(providerId) }
    }

    fun loadTeam(providerId: String) {
        viewModelScope.launch { _team.value = repo.getTeamForEstablishment(providerId) }
    }

    fun loadReviews(providerId: String) {
        viewModelScope.launch { _ratingStats.value = repo.getReviewsStats(providerId) }
    }

    fun createAppointment(appointment: Appointment) {
        _bookingStatus.value = null
        viewModelScope.launch {
            val isTaken = repo.isTimeSlotTaken(appointment.employeeId, appointment.date, appointment.durationMin)
            if (isTaken) {
                _bookingStatus.value = "⚠️ Horário indisponível! Tente outro."
                return@launch
            }
            if (repo.createAppointment(appointment)) {
                _bookingStatus.value = "✅ Agendamento realizado com sucesso!"
            } else {
                _bookingStatus.value = "❌ Erro ao agendar."
            }
        }
    }
}