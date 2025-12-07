package com.example.styloandroid.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val repo = AuthRepository()

    // Enum para definir destinos de navegação de forma tipada
    enum class SplashDestination {
        HOME_MANAGER,
        HOME_CLIENT,
        LOGIN
    }

    private val _navigationEvent = MutableLiveData<SplashDestination>()
    val navigationEvent: LiveData<SplashDestination> = _navigationEvent

    fun checkAuth() {
        viewModelScope.launch {
            // Mantém o delay visual da Splash
            delay(2000)

            val currentUser = repo.currentUserId()
            if (currentUser != null) {
                val user = repo.getAppUser()
                if (user != null) {
                    when (user.role) {
                        "GESTOR", "FUNCIONARIO" -> _navigationEvent.value = SplashDestination.HOME_MANAGER
                        "CLIENTE" -> _navigationEvent.value = SplashDestination.HOME_CLIENT
                        else -> _navigationEvent.value = SplashDestination.LOGIN
                    }
                } else {
                    _navigationEvent.value = SplashDestination.LOGIN
                }
            } else {
                _navigationEvent.value = SplashDestination.LOGIN
            }
        }
    }
}