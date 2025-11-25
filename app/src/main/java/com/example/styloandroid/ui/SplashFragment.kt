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

        // Usa lifecycleScope para rodar em background sem travar a tela
        lifecycleScope.launch {
            // Um pequeno delay para a marca aparecer (opcional, mas fica bonito)
            delay(1500)
            checkAuthStatus()
        }
    }

    private suspend fun checkAuthStatus() {
        // Se o Fragment já morreu (usuário fechou o app), para aqui
        if (!isAdded) return

        val navController = findNavController()

        // 1. Verifica se tem sessão aberta no Firebase Auth
        if (repo.currentUserId() != null) {

            // 2. Busca os dados no Firestore para saber QUEM é (Cliente ou Profissional)
            val user = repo.getAppUser()

            if (user != null) {
                if (user.role == "profissional") {
                    // É Cabeleireiro/Barbeiro -> Painel de Gestão
                    navController.navigate(R.id.action_splash_to_home)
                } else {
                    // É Cliente -> Home de Busca
                    navController.navigate(R.id.action_splash_to_client_home)
                }
            } else {
                // Erro ao ler perfil (internet ruim ou user deletado) -> Vai pro Login
                navController.navigate(R.id.action_splash_to_login)
            }
        } else {
            // Ninguém logado -> Login
            navController.navigate(R.id.action_splash_to_login)
        }
    }
}