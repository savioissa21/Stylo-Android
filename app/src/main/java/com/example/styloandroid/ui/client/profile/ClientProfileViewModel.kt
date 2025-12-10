package com.example.styloandroid.ui.client.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ClientProfileViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _user = MutableLiveData<AppUser?>()
    val user: LiveData<AppUser?> = _user

    private val _statusMsg = MutableLiveData<String?>()
    val statusMsg: LiveData<String?> = _statusMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var newPhotoUri: Uri? = null

    fun loadProfile() {
        viewModelScope.launch {
            _user.value = repo.getAppUser()
        }
    }

    fun selectImage(uri: Uri) {
        newPhotoUri = uri
    }

    fun saveProfile(name: String, phone: String) {
        if (name.isBlank()) {
            _statusMsg.value = "O nome não pode ficar vazio."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val success = repo.updateUserProfile(name, phone, newPhotoUri)

            if (success) {
                _statusMsg.value = "Perfil atualizado com sucesso!"
                newPhotoUri = null
                loadProfile() 
            } else {
                _statusMsg.value = "Erro ao atualizar perfil."
            }
            _isLoading.value = false
        }
    }

    fun clearStatus() { _statusMsg.value = null }
}