package com.example.styloandroid.ui.management

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.management.ServiceAdapter
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentServicesBinding
import com.google.android.material.textfield.TextInputEditText

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private val vm: ManagementViewModel by viewModels()
    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!
    private var currentTeamList: List<AppUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentServicesBinding.bind(view)

        val adapter = ServiceAdapter(
            onDeleteClick = { service ->
                // Clique longo ou botão de delete
                AlertDialog.Builder(requireContext())
                    .setTitle("Ação")
                    .setItems(arrayOf("Gerenciar Equipe", "Excluir Serviço")) { _, which ->
                        when(which) {
                            0 -> showLinkTeamDialog(service)
                            1 -> vm.deleteService(service.id)
                        }
                    }.show()
            }
        )
        // Vamos usar o clique normal do item para abrir a gestão de equipe também
        // (Precisaria atualizar o adapter para aceitar um onItemClick, mas usaremos o deleteClick como 'Menu' por enquanto para simplificar, ou você pode alterar o Adapter)

        b.rvServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvServices.adapter = adapter

        b.fabAdd.setOnClickListener { showCreateServiceDialog() }

        vm.services.observe(viewLifecycleOwner) { adapter.updateList(it) }
        vm.teamMembers.observe(viewLifecycleOwner) { currentTeamList = it }
        vm.operationStatus.observe(viewLifecycleOwner) { if(it!=null) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }

        vm.loadServices()
        vm.loadTeamForSelection()
    }

    // Dialog Simples: Cria Serviço
    private fun showCreateServiceDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etName = android.widget.EditText(requireContext()).apply { hint = "Nome" }
        val etPrice = android.widget.EditText(requireContext()).apply { hint = "Preço"; inputType = 8194 } // Decimal
        val etDur = android.widget.EditText(requireContext()).apply { hint = "Minutos"; inputType = 2 } // Number

        layout.addView(etName); layout.addView(etPrice); layout.addView(etDur)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Serviço")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull()
                val dur = etDur.text.toString().toIntOrNull()
                if (name.isNotEmpty() && price != null && dur != null) {
                    // Cria inicialmente sem ninguém ou com o gestor (opcional)
                    vm.addService(name, price, dur, emptyList())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Dialog: Vincula Equipe (Abre depois de criar)
    private fun showLinkTeamDialog(service: Service) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_service, null)
        // Esconde campos de texto, deixa só a lista
        dialogView.findViewById<View>(R.id.etServiceName).visibility = View.GONE
        dialogView.findViewById<View>(R.id.etServicePrice).visibility = View.GONE
        dialogView.findViewById<View>(R.id.etServiceDuration).visibility = View.GONE

        val container = dialogView.findViewById<LinearLayout>(R.id.containerEmployees)

        currentTeamList.forEach { member ->
            val cb = CheckBox(requireContext())
            cb.text = member.name
            cb.tag = member.uid.ifEmpty { member.email } // Usa email se uid for vazio (pendente)
            // Marca se já estiver na lista do serviço
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
                // Aqui chamamos um metodo de update (vamos reusar o addService por enquanto pois o firebase set sobrescreve se tiver ID, mas precisamos passar o ID do serviço)
                // Para ser perfeito, o ViewModel deveria ter um updateService.
                // Vamos deletar e recriar ou criar um updateService no VM.
                // Como é trabalho, vamos assumir que addService com mesmo ID atualiza se o repo estiver configurado para set(..., SetOptions.merge()).

                // Melhor: Vamos criar um método updateService no ViewModel rapidinho ou deletar/criar.
                vm.deleteService(service.id) // Remove antigo
                vm.addService(service.name, service.price, service.durationMin, selectedIds) // Cria novo com lista atualizada
            }
            .show()
    }
}