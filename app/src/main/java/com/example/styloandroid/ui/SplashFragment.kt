package com.example.styloandroid.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthRepository
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val repo = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Removemos o Handler e usamos coroutines para buscar o usuário
        lifecycleScope.launch {
            checkAuthStatus()
        }
    }

    private suspend fun checkAuthStatus() {
        val navController = findNavController()

        // 1. Verifica se tem usuário logado no Auth
        if (repo.currentUserId() != null) {
            // 2. Busca os dados dele no Firestore para saber o ROLE
            val user = repo.getAppUser()

            if (user != null) {
                if (user.role == "profissional") {
                    navController.navigate(R.id.action_splash_to_home)
                } else {
                    // Se for cliente (ou qualquer outro), vai pra home do cliente
                    navController.navigate(R.id.action_splash_to_client_home)
                }
            } else {
                // Se deu erro ao buscar usuário, manda pro login
                navController.navigate(R.id.action_splash_to_login)
            }
        } else {
            navController.navigate(R.id.action_splash_to_login)
        }
    }
}