package com.example.styloandroid.viewmodel.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AuthRepository
import com.example.styloandroid.data.auth.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableLiveData<AuthResult>()
    val state: LiveData<AuthResult> = _state

    // LiveData para status de reset de senha
    private val _resetStatus = MutableLiveData<String?>()
    val resetStatus: LiveData<String?> = _resetStatus

    fun login(email: String, pass: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || pass.length < 6) {
            _state.value = AuthResult.Error("Credenciais inválidas")
            return
        }
        viewModelScope.launch {
            _state.value = AuthResult.Loading
            _state.value = repo.login(email, pass)
        }
    }

    // NOVO: Lógica de reset
    fun forgotPassword(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetStatus.value = "Digite um e-mail válido."
            return
        }
        viewModelScope.launch {
            val success = repo.sendPasswordReset(email)
            if (success) {
                _resetStatus.value = "E-mail de redefinição enviado!"
            } else {
                _resetStatus.value = "Erro ao enviar e-mail. Verifique se o endereço está correto."
            }
        }
    }

    fun clearResetStatus() { _resetStatus.value = null }
}