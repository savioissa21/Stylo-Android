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

        b.btnLogin.setOnClickListener {
            vm.login(
                b.etEmail.text?.toString().orEmpty(),
                b.etPassword.text?.toString().orEmpty()
            )
        }

        b.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        vm.state.observe(viewLifecycleOwner) { res: AuthState ->
            when (res) {
                is AuthState.Loading -> b.btnLogin.isEnabled = false
                is AuthState.Success -> {
                    b.btnLogin.isEnabled = true
                    // Roteamento baseado nos novos ROLES
                    when (res.role) {
                        "GESTOR" -> findNavController().navigate(R.id.action_login_to_home)
                        "FUNCIONARIO" -> findNavController().navigate(R.id.providerAgendaFragment) // Cria rota direta se precisar
                        "CLIENTE" -> findNavController().navigate(R.id.action_login_to_client_home)
                        
                        // Fallback para versões antigas do app
                        "profissional" -> findNavController().navigate(R.id.action_login_to_home)
                        "cliente" -> findNavController().navigate(R.id.action_login_to_client_home)
                        
                        else -> Toast.makeText(requireContext(), "Tipo de usuário desconhecido.", Toast.LENGTH_SHORT).show()
                    }
                }
                is AuthState.Error -> {
                    b.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}