// app/src/main/java/com/example/styloandroid/ui/home/ClientHomeFragment.kt

package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding
// Use um novo layout (ex: fragment_client_home.xml) para diferenciar

class ClientHomeFragment : Fragment(R.layout.fragment_home) { // Use R.layout.fragment_home temporariamente
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        // Altera o texto para confirmar o redirecionamento
        b.tvWelcome.text = "Bem-vindo Cliente!"

        // Adicione o mesmo logout para o botão "Sair"
        b.btnLogout.setOnClickListener {
            // Lógica de logout (idealmente, use um ClientHomeViewModel)
             findNavController().navigate(R.id.action_client_home_to_login)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}