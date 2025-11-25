package com.example.styloandroid.ui.management

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.styloandroid.R
import com.example.styloandroid.data.management.ServiceAdapter // Vamos criar abaixo
import com.example.styloandroid.databinding.FragmentServicesBinding

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private val vm: ManagementViewModel by viewModels()
    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentServicesBinding.bind(view)

        // Configura Adapter
        val adapter = ServiceAdapter(
            onDeleteClick = { service ->
                // Confirmação simples
                vm.deleteService(service.id)
                Toast.makeText(requireContext(), "Serviço removido", Toast.LENGTH_SHORT).show()
            }
        )
        b.rvServices.adapter = adapter

        // Botão Adicionar (Abre Dialog)
        b.fabAdd.setOnClickListener {
            showAddServiceDialog()
        }

        // Observa dados
        vm.services.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        vm.operationStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Carrega inicial
        vm.loadServices()
    }

    private fun showAddServiceDialog() {
        // Criação manual do layout do dialog para ser rápido
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(requireContext()).apply { hint = "Nome do Serviço (ex: Corte)" }
        val etPrice = EditText(requireContext()).apply { hint = "Preço (ex: 35.00)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etDuration = EditText(requireContext()).apply { hint = "Duração em min (ex: 30)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }

        layout.addView(etName)
        layout.addView(etPrice)
        layout.addView(etDuration)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Serviço")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull()
                val duration = etDuration.text.toString().toIntOrNull()

                if (name.isNotEmpty() && price != null && duration != null) {
                    vm.addService(name, price, duration)
                } else {
                    Toast.makeText(requireContext(), "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}