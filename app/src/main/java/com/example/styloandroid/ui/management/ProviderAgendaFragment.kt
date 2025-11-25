package com.example.styloandroid.ui.management

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentProviderAgendaBinding

class ProviderAgendaFragment : Fragment(R.layout.fragment_provider_agenda) {

    private var _binding: FragmentProviderAgendaBinding? = null
    private val binding get() = _binding!!

    // Usa o ViewModel que criamos no Passo 2
    private val vm: AgendaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProviderAgendaBinding.bind(view)

        // Configura RecyclerView e Adapter
        val adapter = AgendaAdapter(emptyList()) { appointmentId, newStatus ->
            // Quando clicar no botão, chama o ViewModel
            vm.updateStatus(appointmentId, newStatus)
        }

        binding.rvAgenda.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAgenda.adapter = adapter

        // Observa mudanças na lista de agendamentos
        vm.appointments.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "Nenhum agendamento encontrado", Toast.LENGTH_SHORT).show()
            }
            adapter.updateList(list)
        }

        // Observa mensagens de sucesso/erro
        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Carrega os dados iniciais
        vm.loadAppointments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}