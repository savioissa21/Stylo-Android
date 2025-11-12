package com.example.styloandroid.ui.auth // Ou o pacote que você preferir

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthRepository

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val repo = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adiciona um pequeno delay para a splash não ser instantânea
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 1000) // 1 segundo de delay
    }

    private fun checkAuthStatus() {
        // Garante que o fragment ainda está "vivo" antes de navegar
        if (!isAdded) return

        val navController = findNavController()

        if (repo.currentUserId() != null) {
            // Usuário está LOGADO -> Vai para Home
            navController.navigate(R.id.action_splash_to_home)
        } else {
            // Usuário está DESLOGADO -> Vai para Login
            navController.navigate(R.id.action_splash_to_login)
        }
    }
}