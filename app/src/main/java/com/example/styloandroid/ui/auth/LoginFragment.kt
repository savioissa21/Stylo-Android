package com.example.styloandroid.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentLoginBinding
import com.example.styloandroid.data.auth.AuthResult as AuthState

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!
    private val vm: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentLoginBinding.bind(view)

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text?.toString().orEmpty()
            val pass = b.etPassword.text?.toString().orEmpty()

            if (email.isNotBlank() && pass.isNotBlank()) {
                vm.login(email, pass)
            } else {
                Toast.makeText(requireContext(), "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        b.tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidade ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        vm.state.observe(viewLifecycleOwner) { res: AuthState ->
            when (res) {
                is AuthState.Loading -> {
                    b.btnLogin.isEnabled = false
                    b.btnLogin.text = "Entrando..."
                }
                is AuthState.Success -> {
                    b.btnLogin.isEnabled = true
                    b.btnLogin.text = "Entrar"

                    when (res.role) {
                        "GESTOR", "FUNCIONARIO" -> findNavController().navigate(R.id.action_login_to_home)
                        "CLIENTE" -> findNavController().navigate(R.id.action_login_to_client_home)
                        else -> {
                            findNavController().navigate(R.id.action_login_to_client_home)
                        }
                    }
                }
                is AuthState.Error -> {
                    b.btnLogin.isEnabled = true
                    b.btnLogin.text = "Entrar"
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}