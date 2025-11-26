package com.example.styloandroid.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val repo = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(1500)
            checkAuthStatus()
        }
    }

    private suspend fun checkAuthStatus() {
        if (!isAdded) return
        val navController = findNavController()

        if (repo.currentUserId() != null) {
            val user = repo.getAppUser()

            if (user != null) {
                when (user.role) {
                    "GESTOR" -> {
                        // Dono vai para o Painel Completo
                        navController.navigate(R.id.action_splash_to_home)
                    }
                    "FUNCIONARIO" -> {
                        // Funcionário vai direto para a Agenda (Reusando fragmento de agenda)
                        navController.navigate(R.id.providerAgendaFragment)
                    }
                    "CLIENTE" -> {
                        // Cliente vai para busca
                        navController.navigate(R.id.action_splash_to_client_home)
                    }
                    else -> navController.navigate(R.id.action_splash_to_login)
                }
            } else {
                navController.navigate(R.id.action_splash_to_login)
            }
        } else {
            navController.navigate(R.id.action_splash_to_login)
        }
    }
}