package com.example.styloandroid.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels 
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val vm: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        vm.checkAuth()
    }
}