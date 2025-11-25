package com.example.styloandroid.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentLoginBinding
import com.example.styloandroid.viewmodel.auth.LoginViewModel
import com.example.styloandroid.data.auth.AuthResult as AuthState

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!
    private val vm: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentLoginBinding.bind(view)

        // Se já estiver autenticado, pula para Home
        //AuthRepository().currentUserId()?.let {
          //  findNavController().navigate(R.id.action_login_to_home)
            //return
        //}

        // Clique do botão de login
        b.btnLogin.setOnClickListener {
            vm.login(
                b.etEmail.text?.toString().orEmpty(),
                b.etPassword.text?.toString().orEmpty()
            )
        }

        // Navegar para tela de cadastro
        b.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Observar estado do ViewModel
        vm.state.observe(viewLifecycleOwner) { res: AuthState ->
            when (res) {
                is AuthState.Loading -> b.btnLogin.isEnabled = false
                is AuthState.Success -> {
                    b.btnLogin.isEnabled = true
                    when (res.role) {
                        "profissional" -> {
                            // Redireciona para a Home do Estabelecimento (Seu HomeFragment atual)
                            findNavController().navigate(R.id.providerAgendaFragment)
                        }
                        "cliente" -> {
                            // Redireciona para a Home do Cliente (Você precisará criar esse Fragment e destino)
                            findNavController().navigate(R.id.action_login_to_client_home)
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Erro: Tipo de usuário inválido.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is AuthState.Error -> {
                    b.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
