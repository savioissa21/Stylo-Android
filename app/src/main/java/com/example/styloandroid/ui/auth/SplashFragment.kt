package com.example.styloandroid.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthRepository

/**
 * Novo Fragment (Passo 1)
 * Roteia o usuário para Login (se deslogado) ou Home (se logado).
 */
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val repo = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 1200) // Delay de 1.2s
    }

    private fun checkAuthStatus() {
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