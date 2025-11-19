// app/src/main/java/com/example/styloandroid/ui/home/ClientHomeFragment.kt

package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthRepository
import com.example.styloandroid.databinding.FragmentClientHomeBinding // 👈 ESTA É A CLASSE CHAVE QUE DEVE SER GERADA

// Define o layout para o fragmento
class ClientHomeFragment : Fragment(R.layout.fragment_client_home) {

    // Configura o View Binding
    private var _b: FragmentClientHomeBinding? = null
    private val b get() = _b!!

    private val authRepo = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o binding
        _b = FragmentClientHomeBinding.bind(view)

        // 🟢 Acesso aos elementos que estavam dando erro
        b.tvWelcomeClient.text = "Bem-vindo, Cliente!"

        // 🟢 Lógica de Logout
        b.btnLogoutClient.setOnClickListener {
            authRepo.logout()
            findNavController().navigate(R.id.action_client_home_to_login)
        }
    }

    // Limpa a referência de binding para evitar vazamento de memória
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}