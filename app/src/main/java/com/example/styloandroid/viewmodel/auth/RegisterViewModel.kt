package com.example.styloandroid.ui.auth

import android.util.Patterns
import androidx.lifecycle.*
import com.example.styloandroid.data.auth.AuthRepository
import com.example.styloandroid.data.auth.AuthResult
import kotlinx.coroutines.launch


class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableLiveData<AuthResult>()
    val state: LiveData<AuthResult> = _state

    fun register(name: String, email: String, pass: String, confirm: String) {
        when {
            name.isBlank() -> _state.value = AuthResult.Error("Nome obrigatório")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> _state.value = AuthResult.Error("E-mail inválido")
            pass.length < 6 -> _state.value = AuthResult.Error("Senha mínima 6")
            pass != confirm -> _state.value = AuthResult.Error("Senhas não conferem")
            else -> viewModelScope.launch {
                _state.value = AuthResult.Loading
                try {
                    repo.register(name, email, pass)     // ✅ agora não retorna nada
                    // sucesso
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    _state.value = AuthResult.Success(uid)
                } catch (e: Exception) {
                    _state.value = AuthResult.Error(e.message ?: "Falha no registro")
                }
            }
        }
    }
}

