// savioissa21/stylo-android/Stylo-Android-2f9947328211a7c424c8cdf8199117b3fe4515ae/app/src/main/java/com/example/styloandroid/ui/home/HomeViewModel.kt

package com.example.styloandroid.ui.home

import androidx.lifecycle.ViewModel
import com.example.styloandroid.data.auth.AuthRepository

class HomeViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    // Simplificando: a função de logout não precisa de LiveData se for só para deslogar
    // e navegar, pois a navegação será chamada no Fragment.
    fun logout() {
        repo.logout()
    }
}