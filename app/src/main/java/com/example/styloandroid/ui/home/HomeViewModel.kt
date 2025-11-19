// savioissa21/stylo-android/Stylo-Android-2f9947328211a7c424c8cdf8199117b3fe4515ae/app/src/main/java/com/example/styloandroid/ui/home/HomeViewModel.kt

package com.example.styloandroid.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styloandroid.data.auth.AuthRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    // NOVO: LiveData para o nome do usuário
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        // Inicia a busca do nome logo que o ViewModel é criado
        fetchUserName()
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            val user = repo.getAppUser()
            // Se o usuário e o nome existirem, atualiza o LiveData
            _userName.value = user?.name ?: "Usuário"
        }
    }

    // Simplificando: a função de logout não precisa de LiveData se for só para deslogar
    // e navegar, pois a navegação será chamada no Fragment.
    fun logout() {
        repo.logout()
    }
}