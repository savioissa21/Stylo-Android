// ARQUIVO: com.example.styloandroid.presentation.viewmodels/AuthViewModel.kt

package com.example.styloandroid.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.styloandroid.data.services.AuthService

// Definimos os eventos que a Activity deve reagir
sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object RegistrationSuccess : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}

class AuthViewModel : ViewModel() {

    private val authService = AuthService()

    // 1. Estado de Carregamento (Exposto como imutável)
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 2. Eventos (Exposto como imutável - Eventos de navegação/feedback)
    private val _event = MutableLiveData<AuthEvent>()
    val event: LiveData<AuthEvent> = _event

    /**
     * Lógica para tentar o login.
     */
    fun attemptLogin(email: String, password: String) {
        // Validação básica (poderia ser mais complexa)
        if (email.isBlank() || password.isBlank()) {
            _event.value = AuthEvent.Error("Preencha todos os campos para fazer login.")
            return
        }

        _isLoading.value = true

        authService.login(email, password) { result ->
            _isLoading.value = false // Sempre desliga o loading

            when (result) {
                is AuthService.AuthResult.Success -> {
                    _event.value = AuthEvent.LoginSuccess // Dispara evento de sucesso
                }
                is AuthService.AuthResult.Error -> {
                    _event.value = AuthEvent.Error(result.message) // Dispara evento de erro
                }
            }
        }
    }

    /**
     * Lógica para tentar o cadastro.
     */
    fun attemptRegister(email: String, password: String, name: String) {
        // Validação básica
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _event.value = AuthEvent.Error("Preencha todos os campos para se registrar.")
            return
        }

        _isLoading.value = true

        authService.registerClient(email, password, name) { result ->
            _isLoading.value = false
            when (result) {
                is AuthService.AuthResult.Success -> {
                    _event.value = AuthEvent.RegistrationSuccess
                }
                is AuthService.AuthResult.Error -> {
                    _event.value = AuthEvent.Error(result.message)
                }
            }
        }
    }
}