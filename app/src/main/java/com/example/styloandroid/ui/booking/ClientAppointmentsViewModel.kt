package com.example.styloandroid.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import kotlinx.coroutines.launch

class ClientAppointmentsViewModel : ViewModel() {

    private val repo = BookingRepository()

    // Listas separadas
    private val _upcomingList = MutableLiveData<List<Appointment>>()
    private val _historyList = MutableLiveData<List<Appointment>>()

    // Lista que está sendo exibida atualmente
    private val _currentList = MutableLiveData<List<Appointment>>()
    val currentList: LiveData<List<Appointment>> = _currentList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    // Controle da aba selecionada (0 = Próximos, 1 = Histórico)
    var selectedTab = 0

    fun loadAppointments() {
        _isLoading.value = true
        viewModelScope.launch {
            val allAppointments = repo.getClientAppointments()
            val now = System.currentTimeMillis()

            // Lógica de Separação
            val upcoming = mutableListOf<Appointment>()
            val history = mutableListOf<Appointment>()


            allAppointments.forEach { app ->
                // Considera histórico se: Status for cancelado/concluído OU a data já passou
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

            // Atualiza a lista atual baseada na aba selecionada
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
                loadAppointments() // Recarrega para mover para o histórico
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
                loadAppointments() // Recarrega para atualizar status de review
            } else {
                _statusMsg.value = "Erro ao enviar avaliação."
            }
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}