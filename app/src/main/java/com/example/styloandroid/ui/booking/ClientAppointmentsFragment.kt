package com.example.styloandroid.ui.booking

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.databinding.FragmentClientHomeBinding // Usando binding genérico ou crie um layout simples
// Vamos assumir que você crie um layout simples com RecyclerView chamado fragment_simple_list.xml
// Se não, pode reutilizar a estrutura da Agenda.

import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.launch

// OBS: Crie o layout 'fragment_client_appointments.xml' com:
// 1 RecyclerView (@+id/rvAppointments)
// 1 ProgressBar (@+id/progressBar)
// 1 TextView (@+id/tvEmpty) "Você não tem agendamentos"

class ClientAppointmentsFragment : Fragment(R.layout.fragment_client_appointments) {

    private val repo = BookingRepository()
    private val adapter = ClientAppointmentsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvAppointments)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Busca dados
        progress.isVisible = true
        lifecycleScope.launch {
            val list = repo.getClientAppointments()
            progress.isVisible = false

            if (list.isEmpty()) {
                tvEmpty.isVisible = true
                rv.isVisible = false
            } else {
                tvEmpty.isVisible = false
                rv.isVisible = true
                adapter.updateList(list)
            }
        }
    }
}