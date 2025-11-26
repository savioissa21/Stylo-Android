package com.example.styloandroid.ui.management

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service // Apenas para reuso do adapter, se quiser
import com.example.styloandroid.databinding.FragmentTeamManagementBinding

class TeamManagementFragment : Fragment(R.layout.fragment_team_management) {

    private var _binding: FragmentTeamManagementBinding? = null
    private val binding get() = _binding!!
    private val vm: TeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeamManagementBinding.bind(view)

        // Setup RecyclerView (Pode precisar de um Adapter específico para AppUser depois)
        // Por enquanto, vamos deixar a estrutura pronta
        binding.rvTeam.layoutManager = LinearLayoutManager(requireContext())
        // binding.rvTeam.adapter = TeamAdapter(...) -> Crie este adapter se quiser listar nomes

        binding.btnInvite.setOnClickListener {
            val email = binding.etEmailInvite.text.toString()
            vm.sendInvite(email)
            binding.etEmailInvite.text?.clear()
        }

        vm.inviteStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                vm.clearStatus()
            }
        }

        vm.teamList.observe(viewLifecycleOwner) { team ->
            binding.tvEmptyTeam.isVisible = team.isEmpty()
            // adapter.submitList(team)
        }
        
        vm.loadTeam()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}