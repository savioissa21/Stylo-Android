package com.example.styloandroid.ui.management

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.ServiceAdapter
import com.example.styloandroid.databinding.FragmentServicesBinding
import com.google.android.material.textfield.TextInputEditText

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private val vm: ManagementViewModel by viewModels()
    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!
    
    // Cache da lista de equipe para usar no dialog
    private var currentTeamList: List<AppUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentServicesBinding.bind(view)

        val adapter = ServiceAdapter(
            onDeleteClick = { service ->
                vm.deleteService(service.id)
            }
        )
        
        b.rvServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvServices.adapter = adapter

        b.fabAdd.setOnClickListener {
            showAddServiceDialog()
        }

        // Observa Serviços
        vm.services.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        // Observa Equipe (carregada em background)
        vm.teamMembers.observe(viewLifecycleOwner) { team ->
            currentTeamList = team
        }

        vm.operationStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Carregamentos Iniciais
        vm.loadServices()
        vm.loadTeamForSelection()
    }

    private fun showAddServiceDialog() {
        // Infla o layout personalizado bonito
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_service, null)
        
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etServiceName)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etServicePrice)
        val etDuration = dialogView.findViewById<TextInputEditText>(R.id.etServiceDuration)
        val container = dialogView.findViewById<LinearLayout>(R.id.containerEmployees)
        val tvNoTeam = dialogView.findViewById<TextView>(R.id.tvNoTeam)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            
        // Popula os checkboxes com a equipe
        if (currentTeamList.isEmpty()) {
            tvNoTeam.visibility = View.VISIBLE
        } else {
            currentTeamList.forEach { member ->
                val cb = CheckBox(requireContext())
                cb.text = if (member.role == "GESTOR") "${member.name} (Você)" else member.name
                cb.tag = member.uid // Guarda o ID na tag
                cb.isChecked = true // Marcado por padrão
                container.addView(cb)
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val price = etPrice.text.toString().toDoubleOrNull()
            val duration = etDuration.text.toString().toIntOrNull()

            // Coleta os IDs selecionados
            val selectedIds = mutableListOf<String>()
            container.children.forEach { view ->
                if (view is CheckBox && view.isChecked) {
                    selectedIds.add(view.tag.toString())
                }
            }

            if (name.isNotEmpty() && price != null && duration != null) {
                if (selectedIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Selecione pelo menos um profissional", Toast.LENGTH_SHORT).show()
                } else {
                    vm.addService(name, price, duration, selectedIds)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }
}