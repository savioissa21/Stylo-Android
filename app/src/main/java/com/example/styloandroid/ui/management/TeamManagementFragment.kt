package com.example.styloandroid.ui.management

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.databinding.FragmentTeamManagementBinding
import com.google.android.material.textfield.TextInputEditText

class TeamManagementFragment : Fragment(R.layout.fragment_team_management) {

    private var _binding: FragmentTeamManagementBinding? = null
    private val binding get() = _binding!!
    private val vm: TeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeamManagementBinding.bind(view)

        // Adapter simplificado para mostrar nomes (inline para agilizar)
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var items: List<AppUser> = emptyList()

            override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_2, p, false)
                return object : RecyclerView.ViewHolder(v) {}
            }
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                val user = items[pos]
                val text1 = h.itemView.findViewById<TextView>(android.R.id.text1)
                val text2 = h.itemView.findViewById<TextView>(android.R.id.text2)
                text1.text = user.name
                text2.text = user.email + " - " + (if(user.uid.isEmpty()) "Pendente" else "Ativo")
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

    private fun showCreateEmployeeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_employee, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEmpName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmpEmail)
        val etPass = dialogView.findViewById<TextInputEditText>(R.id.etEmpPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            vm.createEmployee(name, email, pass)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}