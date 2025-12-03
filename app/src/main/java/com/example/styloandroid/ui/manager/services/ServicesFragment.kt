package com.example.styloandroid.ui.manager.services

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.ui.manager.services.ServiceAdapter
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentServicesBinding
import com.example.styloandroid.ui.manager.services.ManagementViewModel

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private val vm: ManagementViewModel by viewModels()
    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!

    // Adapter agora instanciado como variável de classe para podermos acessar depois
    private lateinit var adapter: ServiceAdapter

    private var currentTeamList: List<AppUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentServicesBinding.bind(view)

        // Configura Adapter
        adapter = ServiceAdapter(
            onEditClick = { service -> showServiceDialog(service) },
            onDeleteClick = { service ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Excluir Serviço")
                    .setMessage("Tem certeza que deseja excluir '${service.name}'?")
                    .setPositiveButton("Excluir") { _, _ -> vm.deleteService(service.id) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        b.rvServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvServices.adapter = adapter

        // Clique no FAB (Criar)
        b.fabAdd.setOnClickListener { showServiceDialog(null) }

        // --- OBSERVERS ---

        // 1. Observa se é ReadOnly (Funcionário)
        vm.isReadOnly.observe(viewLifecycleOwner) { readOnly ->
            // Esconde o botão de Adicionar se for funcionário
            b.fabAdd.isVisible = !readOnly

            // Avisa o adapter para esconder as lixeiras
            adapter.setReadOnly(readOnly)
        }

        // 2. Lista de Serviços
        vm.services.observe(viewLifecycleOwner) {
            adapter.updateList(it)
            // Feedback visual se a lista estiver vazia (opcional)
            if (it.isEmpty()) {
                Toast.makeText(context, "Nenhum serviço encontrado.", Toast.LENGTH_SHORT).show()
            }
        }

        vm.teamMembers.observe(viewLifecycleOwner) { currentTeamList = it }
        vm.operationStatus.observe(viewLifecycleOwner) { if(it!=null) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

        // Carrega os dados
        vm.loadServices()
        vm.loadTeamForSelection()
    }

    // (Mantenha showServiceDialog e showLinkTeamDialog iguais ao que você já tem)
    private fun showServiceDialog(serviceToEdit: Service?) {
        val isEditing = serviceToEdit != null
        val title = if (isEditing) "Editar Serviço" else "Novo Serviço"

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(requireContext()).apply {
            hint = "Nome"
            setText(serviceToEdit?.name ?: "")
        }
        val etPrice = EditText(requireContext()).apply {
            hint = "Preço (Ex: 35.00)"
            inputType = 8194 // Decimal
            setText(serviceToEdit?.price?.toString() ?: "")
        }
        val etDur = EditText(requireContext()).apply {
            hint = "Duração (minutos)"
            inputType = 2 // Number
            setText(serviceToEdit?.durationMin?.toString() ?: "")
        }

        layout.addView(etName); layout.addView(etPrice); layout.addView(etDur)

        if (isEditing) {
            val btnTeam = Button(requireContext()).apply {
                text = "Gerenciar Equipe deste Serviço"
                setOnClickListener { showLinkTeamDialog(serviceToEdit!!) }
            }
            layout.addView(btnTeam)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull()
                val dur = etDur.text.toString().toIntOrNull()

                if (name.isNotEmpty() && price != null && dur != null) {
                    if (isEditing) {
                        val updated = serviceToEdit!!.copy(name = name, price = price, durationMin = dur)
                        vm.updateService(updated)
                    } else {
                        vm.addService(name, price, dur, emptyList())
                    }
                } else {
                    Toast.makeText(requireContext(), "Preencha valores válidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLinkTeamDialog(service: Service) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_service, null)
        dialogView.findViewById<View>(R.id.etServiceName).visibility = View.GONE
        dialogView.findViewById<View>(R.id.etServicePrice).visibility = View.GONE
        dialogView.findViewById<View>(R.id.etServiceDuration).visibility = View.GONE

        val container = dialogView.findViewById<LinearLayout>(R.id.containerEmployees)
        currentTeamList.forEach { member ->
            val cb = CheckBox(requireContext())
            cb.text = member.name
            cb.tag = member.uid.ifEmpty { member.email }
            cb.isChecked = service.employeeIds.contains(cb.tag.toString())
            container.addView(cb)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Quem faz '${service.name}'?")
            .setView(dialogView)
            .setPositiveButton("Atualizar") { _, _ ->
                val selectedIds = mutableListOf<String>()
                container.children.forEach {
                    if (it is CheckBox && it.isChecked) selectedIds.add(it.tag.toString())
                }
                vm.updateServiceTeam(service.id, selectedIds)
            }
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}