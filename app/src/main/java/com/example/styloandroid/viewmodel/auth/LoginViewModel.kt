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
}
