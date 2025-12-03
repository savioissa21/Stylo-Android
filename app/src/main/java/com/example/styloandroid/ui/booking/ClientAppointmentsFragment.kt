package com.example.styloandroid.ui.booking

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import com.example.styloandroid.databinding.FragmentClientAppointmentsBinding

class ClientAppointmentsFragment : Fragment(R.layout.fragment_client_appointments) {

    private val vm: ClientAppointmentsViewModel by viewModels()
    private lateinit var adapter: ClientAppointmentsAdapter
    private var _binding: FragmentClientAppointmentsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientAppointmentsBinding.bind(view)

        setupAdapter()
        setupListeners()
        setupObservers()

        // Carrega dados iniciais
        vm.loadAppointments()
    }

    private fun setupAdapter() {
        adapter = ClientAppointmentsAdapter(
            onRateClick = { appointment -> showRateDialog(appointment) },
            onCancelClick = { appointment -> showCancelConfirmation(appointment) }
        )
        binding.rvAppointments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppointments.adapter = adapter
    }

    private fun setupListeners() {
        // Listener do Toggle Button (Abas)
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnUpcoming -> vm.selectTab(0)
                    R.id.btnHistory -> vm.selectTab(1)
                }
            }
        }
    }

    private fun setupObservers() {
        vm.currentList.observe(viewLifecycleOwner) { list ->
            binding.tvEmpty.isVisible = list.isEmpty()
            binding.rvAppointments.isVisible = list.isNotEmpty()
            adapter.updateList(list)
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        vm.statusMsg.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                vm.clearStatus()
            }
        }
    }

    private fun showCancelConfirmation(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Agendamento")
            .setMessage("Tem certeza que deseja cancelar o serviço de ${appointment.serviceName}?")
            .setNegativeButton("Não", null)
            .setPositiveButton("Sim, Cancelar") { _, _ ->
                vm.cancelAppointment(appointment)
            }
            .show()
    }

    private fun showRateDialog(appointment: Appointment) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rate_service, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitReview)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString()

            if (rating == 0f) {
                Toast.makeText(requireContext(), "Selecione uma nota.", Toast.LENGTH_SHORT).show()
            } else {
                val review = Review(
                    appointmentId = appointment.id,
                    providerId = appointment.providerId,
                    employeeId = appointment.employeeId,
                    clientId = appointment.clientId,
                    clientName = appointment.clientName,
                    rating = rating,
                    comment = comment
                )
                vm.submitReview(review)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}