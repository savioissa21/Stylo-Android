package com.example.styloandroid.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Importante: use by viewModels()
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    // Injeta o ViewModel
    private val vm: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observa a decisão de navegação do ViewModel
        vm.navigationEvent.observe(viewLifecycleOwner) { destination ->
            val navController = findNavController()
            when (destination) {
                SplashViewModel.SplashDestination.HOME_MANAGER ->
                    navController.navigate(R.id.action_splash_to_home)
                SplashViewModel.SplashDestination.HOME_CLIENT ->
                    navController.navigate(R.id.action_splash_to_client_home)
                SplashViewModel.SplashDestination.LOGIN ->
                    navController.navigate(R.id.action_splash_to_login)
            }
        }

        // Inicia a verificação
        vm.checkAuth()
    }
}