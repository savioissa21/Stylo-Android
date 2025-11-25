package com.example.styloandroid.ui.management

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentServicesBinding

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!
    private val vm: ManagementViewModel by viewModels()
    private val adapter = ServiceAdapter { id -> vm.deleteService(id) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentServicesBinding.bind(view)

        b.rvServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvServices.adapter = adapter

        // Observa dados
        vm.services.observe(viewLifecycleOwner) { list ->
            adapter.update(list)
        }

        vm.operationStatus.observe(viewLifecycleOwner) { msg ->
            if(msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Carrega dados iniciais
        vm.loadServices()

        // Botão Adicionar
        b.fabAdd.setOnClickListener {
            showAddServiceDialog()
        }
    }

    private fun showAddServiceDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(requireContext()).apply { hint = "Nome do Serviço (ex: Corte)" }
        val etPrice = EditText(requireContext()).apply { hint = "Preço (ex: 35.00)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etDuration = EditText(requireContext()).apply { hint = "Duração (minutos)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }

        layout.addView(etName)
        layout.addView(etPrice)
        layout.addView(etDuration)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Serviço")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val duration = etDuration.text.toString().toIntOrNull() ?: 30

                if (name.isNotBlank()) {
                    vm.addService(name, price, duration)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}