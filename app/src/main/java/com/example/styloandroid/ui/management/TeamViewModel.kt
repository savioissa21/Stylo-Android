package com.example.styloandroid.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.TeamRepository
import kotlinx.coroutines.launch

class TeamViewModel : ViewModel() {
    private val repo = TeamRepository()

    private val _teamList = MutableLiveData<List<AppUser>>()
    val teamList: LiveData<List<AppUser>> = _teamList

    private val _inviteStatus = MutableLiveData<String?>()
    val inviteStatus: LiveData<String?> = _inviteStatus

    fun loadTeam() {
        viewModelScope.launch {
            _teamList.value = repo.getMyTeam()
        }
    }

    fun sendInvite(email: String) {
        if (email.isBlank()) {
            _inviteStatus.value = "Por favor, digite um e-mail."
            return
        }
        
        viewModelScope.launch {
            val success = repo.inviteEmployee(email)
            if (success) {
                _inviteStatus.value = "Convite enviado com sucesso! ✅"
            } else {
                _inviteStatus.value = "Erro ao enviar convite. Tente novamente."
            }
        }
    }
    
    // Limpa a mensagem após exibir
    fun clearStatus() {
        _inviteStatus.value = null
    }
}