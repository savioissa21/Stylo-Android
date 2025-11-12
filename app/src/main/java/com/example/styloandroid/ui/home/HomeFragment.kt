// savioissa21/stylo-android/Stylo-Android-2f9947328211a7c424c8cdf8199117b3fe4515ae/app/src/main/java/com/example/styloandroid/ui/home/HomeFragment.kt

package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Importe este
import androidx.navigation.fragment.findNavController // Importe este
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    // 1. Instanciar o ViewModel
    private val vm: HomeViewModel by viewModels() // Use by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        // 2. Implementar a lógica do botão de logout
        b.btnLogout.setOnClickListener {
            vm.logout() // Chama a função que limpa o estado do Firebase Auth

            // 3. Navegar de volta para a tela de Login
            // Vamos precisar adicionar a action 'action_home_to_login' no nav_graph
            findNavController().navigate(R.id.action_home_to_login)
        }

        // TODO: Em um desenvolvimento mais completo, você buscará os dados do usuário aqui
        // Exemplo: b.tvWelcome.text = "Bem-vindo, ${vm.getUserName()}" 
    }

    // Seu método onDestroyView está correto
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}