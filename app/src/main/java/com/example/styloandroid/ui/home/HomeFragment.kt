package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        // Navegação para a Agenda (Já criamos)
        b.cardAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_agenda)
        }

        // Navegação para Serviços (Vamos criar agora)
        b.cardServices.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_services)
        }

        b.cardProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Perfil em construção...", Toast.LENGTH_SHORT).show()
        }

        // Logout
        b.cardLogout.setOnClickListener {
            vm.logout()
            findNavController().navigate(R.id.action_home_to_login)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}