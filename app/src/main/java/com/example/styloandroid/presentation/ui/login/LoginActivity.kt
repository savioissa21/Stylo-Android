// ARQUIVO: com.example.styloandroid.presentation.ui.login/LoginActivity.kt

package com.example.styloandroid.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.styloandroid.MainActivity
import com.example.styloandroid.databinding.ActivityLoginBinding // Import do View Binding
import com.example.styloandroid.presentation.viewmodels.AuthViewModel
import com.example.styloandroid.presentation.viewmodels.AuthEvent

class LoginActivity : AppCompatActivity() {

    // 1. View Binding - Acesso seguro aos elementos da UI
    private lateinit var binding: ActivityLoginBinding

    // 2. ViewModel - Inicializa o ViewModel usando KTX
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    /**
     * Configura o que acontece quando os botões são clicados.
     * Ação: A View chama a função de lógica no ViewModel.
     */
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 📞 Chamada ao ViewModel para iniciar a lógica
            viewModel.attemptLogin(email, password)
        }

        // Adicionar listener para ir para a tela de Cadastro, se tiver
        // binding.btnRegister.setOnClickListener {
        //     startActivity(Intent(this, RegisterActivity::class.java))
        // }
    }


    private fun setupObservers() {
        // Observa o estado de Carregamento (Loading)
        viewModel.isLoading.observe(this, Observer { isLoading ->
            // Se estiver carregando, mostra a barra de progresso e desabilita o botão
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        })

        // Observa os Eventos (Sucesso, Erro, Navegação)
        viewModel.event.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is AuthEvent.LoginSuccess -> {
                        Toast.makeText(this, "Bem-vindo!", Toast.LENGTH_SHORT).show()
                        // Navegação de sucesso
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    is AuthEvent.Error -> {
                        // Exibe a mensagem de erro da lógica do Firebase/ViewModel
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
                    // Adicionar AuthEvent.RegistrationSuccess se a View for para Registro
                    else -> {}
                }
            }
        })
    }
}