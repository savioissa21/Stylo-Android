package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        // Cumprimento
        val name = auth.currentUser?.displayName?.trim().orEmpty()
        b.toolbar.title = if (name.isNotEmpty()) "Olá, $name" else "Bem-vindo ao Stylo"

        // Menu (logout)
        b.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    // volta pro Login limpando backstack
                    val opts = androidx.navigation.navOptions {
                        popUpTo(R.id.homeFragment) { inclusive = true }
                    }
                    findNavController().navigate(R.id.loginFragment, null, opts)
                    true
                }
                else -> false
            }
        }

        // Grade de recursos
        val features = listOf(
            FeatureItem("book", "Agendar", R.drawable.ic_event_24),
            FeatureItem("my_appointments", "Meus agendamentos", R.drawable.ic_list_24),
            FeatureItem("services", "Serviços", R.drawable.ic_cut_24),
            FeatureItem("professionals", "Profissionais", R.drawable.ic_people_24),
            FeatureItem("profile", "Perfil", R.drawable.ic_person_24)
        )

        b.rvFeatures.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvFeatures.adapter = FeatureAdapter(features) { item ->
            when (item.id) {
                "book" -> navigateSafe(R.id.servicesFragment) // ou Booking direto, se preferir
                "my_appointments" -> navigateSafe(R.id.appointmentsFragment)
                "services" -> navigateSafe(R.id.servicesFragment)
                "professionals" -> navigateSafe(R.id.professionalsFragment)
                "profile" -> navigateSafe(R.id.profileFragment)
                else -> Toast.makeText(requireContext(), "Em breve", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateSafe(dest: Int) {
        runCatching { findNavController().navigate(dest) }
            .onFailure { Toast.makeText(requireContext(), "Rota indisponível no nav_graph", Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
