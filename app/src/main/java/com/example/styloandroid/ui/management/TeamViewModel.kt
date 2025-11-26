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

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    fun loadTeam() {
        viewModelScope.launch {
            _teamList.value = repo.getMyTeam()
        }
    }

    fun createEmployee(name: String, email: String, pass: String) {
        if (name.isBlank() || email.isBlank() || pass.length < 6) {
            _statusMsg.value = "Preencha tudo corretamente. Senha min 6 caracteres."
            return
        }

        viewModelScope.launch {
            val success = repo.createEmployeeAccount(name, email, pass)
            if (success) {
                _statusMsg.value = "Funcionário criado! Passe a senha para ele."
                loadTeam()
            } else {
                _statusMsg.value = "Erro ao criar funcionário."
            }
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}