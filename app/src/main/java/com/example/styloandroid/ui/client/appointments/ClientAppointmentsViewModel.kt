package com.example.styloandroid.ui.client.appointments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import com.example.styloandroid.data.repository.BookingRepository
import kotlinx.coroutines.launch

class ClientAppointmentsViewModel : ViewModel() {

    private val repo = BookingRepository()

    private val _upcomingList = MutableLiveData<List<Appointment>>()
    private val _historyList = MutableLiveData<List<Appointment>>()

    private val _currentList = MutableLiveData<List<Appointment>>()
    val currentList: LiveData<List<Appointment>> = _currentList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    var selectedTab = 0

    fun loadAppointments() {
        _isLoading.value = true
        viewModelScope.launch {
            val allAppointments = repo.getClientAppointments()
            val now = System.currentTimeMillis()

            val upcoming = mutableListOf<Appointment>()
            val history = mutableListOf<Appointment>()


            allAppointments.forEach { app ->
                val isFinished = app.status == "finished" || app.status == "canceled"
                val isPastDate = app.date < now

                if (isFinished || isPastDate) {
                    history.add(app)
                } else {
                    upcoming.add(app)
                }
            }

            _upcomingList.value = upcoming
            _historyList.value = history

            updateCurrentList()
            _isLoading.value = false
        }
    }

    fun selectTab(index: Int) {
        selectedTab = index
        updateCurrentList()
    }

    private fun updateCurrentList() {
        if (selectedTab == 0) {
            _currentList.value = _upcomingList.value ?: emptyList()
        } else {
            _currentList.value = _historyList.value ?: emptyList()
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = repo.cancelAppointment(appointment.id)
            if (success) {
                _statusMsg.value = "Agendamento cancelado com sucesso."
                loadAppointments()
            } else {
                _statusMsg.value = "Erro ao cancelar."
                _isLoading.value = false
            }
        }
    }

    fun submitReview(review: Review) {
        viewModelScope.launch {
            val success = repo.submitReview(review)
            if (success) {
                _statusMsg.value = "Avaliação enviada! Obrigado. ⭐"
                loadAppointments() 
            } else {
                _statusMsg.value = "Erro ao enviar avaliação."
            }
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}