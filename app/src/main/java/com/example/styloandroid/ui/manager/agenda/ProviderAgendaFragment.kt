package com.example.styloandroid.ui.manager.agenda

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentProviderAgendaBinding
import com.example.styloandroid.ui.manager.agenda.AgendaAdapter
import com.example.styloandroid.ui.manager.agenda.AgendaViewModel

class ProviderAgendaFragment : Fragment(R.layout.fragment_provider_agenda) {

    private var _binding: FragmentProviderAgendaBinding? = null
    private val binding get() = _binding!!

    private val vm: AgendaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProviderAgendaBinding.bind(view)

        val adapter = AgendaAdapter(emptyList()) { appointmentId, newStatus ->
            vm.updateStatus(appointmentId, newStatus)
        }

        binding.rvAgenda.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAgenda.adapter = adapter

        vm.appointments.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "Nenhum agendamento encontrado", Toast.LENGTH_SHORT).show()
            }
            adapter.updateList(list)
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        vm.loadAppointments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}