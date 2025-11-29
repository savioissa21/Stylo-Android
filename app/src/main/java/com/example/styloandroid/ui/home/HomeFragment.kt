package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        setupObservers()
        setupClickListeners()

        // Carrega os dados atualizados ao entrar na tela
        vm.loadDashboardData()
    }

    private fun setupObservers() {
        // 1. Nome e Saudação
        vm.userName.observe(viewLifecycleOwner) { name ->
            b.tvGreeting.text = "Olá, $name"
        }
        vm.businessName.observe(viewLifecycleOwner) { business ->
            b.tvBusinessName.text = business
        }
        
        // 2. Métricas do Dia
        vm.todayCount.observe(viewLifecycleOwner) { count ->
            b.tvCountToday.text = count.toString()
        }
        
        vm.todayRevenue.observe(viewLifecycleOwner) { value ->
            val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            b.tvRevenueToday.text = format.format(value)
        }

        // 3. Próximo Cliente
        vm.nextAppointment.observe(viewLifecycleOwner) { app ->
            if (app != null) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                b.tvNextTime.text = sdf.format(app.date)
                b.tvNextClient.text = app.clientName
                b.tvNextService.text = app.serviceName
                
                // Habilita botão para ir direto pra agenda clicando no card
                b.cardNextClient.setOnClickListener { 
                    findNavController().navigate(R.id.action_home_to_agenda) 
                }
            } else {
                b.tvNextTime.text = "--:--"
                b.tvNextClient.text = "Tudo tranquilo"
                b.tvNextService.text = "Nenhum agendamento próximo"
                b.cardNextClient.setOnClickListener(null)
            }
        }
    }

    private fun setupClickListeners() {
        // Card do Próximo Cliente (Botão ver agenda)
        b.btnViewAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_agenda)
        }

        // Menu Grid (cardAgenda removido pois não existe mais no XML)
        b.cardServices.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_services)
        }
        b.cardTeam.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_teamManagement)
        }

        // Logout
        b.cardLogout.setOnClickListener {
            vm.logout()
            findNavController().navigate(R.id.action_home_to_login)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}