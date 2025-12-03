package com.example.styloandroid.ui.manager.team

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.AppUser
import com.example.styloandroid.databinding.FragmentTeamManagementBinding
import com.example.styloandroid.ui.manager.team.TeamViewModel
import com.google.android.material.textfield.TextInputEditText

class TeamManagementFragment : Fragment(R.layout.fragment_team_management) {

    private var _binding: FragmentTeamManagementBinding? = null
    private val binding get() = _binding!!
    private val vm: TeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.loadServicesForDialog()
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeamManagementBinding.bind(view)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var items: List<AppUser> = emptyList()

            override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_2, p, false)
                return object : RecyclerView.ViewHolder(v) {}
            }

            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                val user = items[pos]
                val text1 = h.itemView.findViewById<TextView>(android.R.id.text1)
                val text2 = h.itemView.findViewById<TextView>(android.R.id.text2)

                text1.text = user.name
                val status = if(user.uid.isEmpty()) "Pendente" else "Ativo"
                text2.text = "${user.email} - $status"

                // CLIQUE NO ITEM -> OPÇÕES
                h.itemView.setOnClickListener {
                    showEmployeeOptions(user)
                }
            }
            override fun getItemCount() = items.size
        }

        binding.rvTeam.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeam.adapter = adapter

        // Botão Adicionar abre o Modal
        binding.btnInvite.setOnClickListener {
            showCreateEmployeeDialog()
        }
        // Ajuste do texto do botão
        binding.btnInvite.text = "Adicionar Funcionário"
        binding.tvInviteLabel.visibility = View.GONE // Esconde label antigo
        binding.tilEmailInvite.visibility = View.GONE // Esconde input antigo

        vm.teamList.observe(viewLifecycleOwner) { team ->
            binding.tvEmptyTeam.isVisible = team.isEmpty()
            adapter.items = team
            adapter.notifyDataSetChanged()
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                vm.clearStatus()
            }
        }

        vm.loadTeam()
    }

    private fun showEmployeeOptions(user: AppUser) {
        val options = mutableListOf<String>()

        // Se for pendente, permite editar nome. Se for ativo, apenas remover.
        if (user.uid.isEmpty()) {
            options.add("Editar Nome")
        }
        options.add("Remover da Equipe")

        AlertDialog.Builder(requireContext())
            .setTitle("Gerenciar: ${user.name}")
            .setItems(options.toTypedArray()) { _, which ->
                val selected = options[which]
                when (selected) {
                    "Remover da Equipe" -> {
                        confirmRemoval(user)
                    }
                    "Editar Nome" -> {
                        showEditNameDialog(user)
                    }
                }
            }
            .show()
    }

    private fun confirmRemoval(user: AppUser) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remover Funcionário")
            .setMessage("Tem certeza que deseja remover ${user.name}? Ele perderá o acesso aos dados da loja.")
            .setPositiveButton("Sim, Remover") { _, _ ->
                vm.removeEmployee(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditNameDialog(user: AppUser) {
        val input = EditText(requireContext())
        input.setText(user.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Nome (Pendente)")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                vm.updatePendingName(user, input.text.toString())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCreateEmployeeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_employee, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEmpName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmpEmail)
        val etPass = dialogView.findViewById<TextInputEditText>(R.id.etEmpPassword)

        // Container onde vamos colocar os checkboxes
        val containerServices = dialogView.findViewById<LinearLayout>(R.id.containerServicesDialog)
        val availableServices = vm.servicesList.value ?: emptyList()

        // Cria um CheckBox para cada serviço dinamicamente
        availableServices.forEach { service ->
            val cb = CheckBox(requireContext())
            cb.text = service.name
            cb.tag = service.id // Guardamos o ID no tag
            containerServices.addView(cb)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            // Coleta os IDs dos serviços marcados
            val selectedIds = mutableListOf<String>()
            val count = containerServices.childCount
            for (i in 0 until count) {
                val v = containerServices.getChildAt(i)
                if (v is CheckBox && v.isChecked) {
                    selectedIds.add(v.tag.toString())
                }
            }

            // Manda tudo para o ViewModel
            vm.createEmployee(name, email, pass, selectedIds)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}