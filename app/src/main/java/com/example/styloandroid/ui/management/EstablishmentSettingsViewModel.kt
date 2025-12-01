package com.example.styloandroid.ui.management

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.EstablishmentRepository
import kotlinx.coroutines.launch

class EstablishmentSettingsViewModel : ViewModel() {
    private val repo = EstablishmentRepository()

    private val _currentUser = MutableLiveData<AppUser?>()
    val currentUser: LiveData<AppUser?> = _currentUser

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadCurrentSettings() {
        viewModelScope.launch {
            _currentUser.value = repo.getMyProfile()
        }
    }

    fun updateProfileImage(uri: Uri) {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Upload para o Storage
            val url = repo.uploadProfileImage(uri)
            if (url != null) {
                // 2. Atualiza o Firestore
                val success = repo.updateUserPhotoUrl(url)
                if (success) {
                    _statusMsg.value = "Foto atualizada com sucesso!"
                    loadCurrentSettings() // Recarrega para mostrar a nova foto
                } else {
                    _statusMsg.value = "Erro ao salvar URL da foto."
                }
            } else {
                _statusMsg.value = "Erro no upload da imagem."
            }
            _isLoading.value = false
        }
    }

    fun saveSettings(openTime: String, closeTime: String, workDays: List<Int>) {
        if (workDays.isEmpty()) {
            _statusMsg.value = "Selecione pelo menos um dia de funcionamento."
            return
        }

        viewModelScope.launch {
            val success = repo.updateEstablishmentSettings(openTime, closeTime, workDays)
            if (success) {
                _statusMsg.value = "Configurações salvas com sucesso!"
            } else {
                _statusMsg.value = "Erro ao salvar."
            }
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}