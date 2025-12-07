package com.example.styloandroid.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentLoginBinding
import com.example.styloandroid.ui.auth.LoginViewModel
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

            if(email.isNotBlank() && pass.isNotBlank()){
                vm.login(email, pass)
            } else {
                Toast.makeText(requireContext(), "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // --- LÓGICA DE ESQUECI MINHA SENHA ---
        b.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        vm.resetStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                vm.clearResetStatus()
            }
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
                        "GESTOR" -> findNavController().navigate(R.id.action_login_to_home)
                        "FUNCIONARIO" -> findNavController().navigate(R.id.action_login_to_home)
                        "CLIENTE" -> findNavController().navigate(R.id.action_login_to_client_home)
                        // Fallbacks
                        "profissional" -> findNavController().navigate(R.id.action_login_to_home)
                        "cliente" -> findNavController().navigate(R.id.action_login_to_client_home)
                        else -> Toast.makeText(requireContext(), "Tipo de usuário desconhecido.", Toast.LENGTH_SHORT).show()
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

    private fun showForgotPasswordDialog() {
        val input = EditText(requireContext())
        input.hint = "Digite seu e-mail"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        // Preenche automaticamente se o usuário já digitou no campo de login
        input.setText(b.etEmail.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Recuperar Senha")
            .setMessage("Enviaremos um link para você redefinir sua senha.")
            .setView(input) // Adiciona margens seria ideal, mas simplificando
            .setPositiveButton("Enviar") { _, _ ->
                val email = input.text.toString()
                vm.forgotPassword(email)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}