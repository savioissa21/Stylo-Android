package com.example.styloandroid.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AuthResult
import com.example.styloandroid.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!
    private val vm: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentRegisterBinding.bind(view)

        b.btnRegister.setOnClickListener {
            val name = b.etName.text?.toString().orEmpty()
            val email = b.etEmail.text?.toString().orEmpty()
            val pass = b.etPassword.text?.toString().orEmpty()
            val confirm = b.etConfirm.text?.toString().orEmpty()

            Log.d("REG_DEBUG", "name='$name' email='$email' pass='${if(pass.isEmpty()) "<empty>" else "<present>"}' confirm='${if(confirm.isEmpty()) "<empty>" else "<present>"}'")

            vm.register(name, email, pass, confirm)
        }

        vm.state.observe(viewLifecycleOwner) { res ->
            when (res) {
                is AuthResult.Loading -> b.btnRegister.isEnabled = false
                is AuthResult.Success -> {
                    b.btnRegister.isEnabled = true
                    findNavController().navigate(R.id.homeFragment)
                }
                is AuthResult.Error -> {
                    b.btnRegister.isEnabled = true
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
