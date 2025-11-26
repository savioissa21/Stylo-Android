package com.example.styloandroid.ui.booking

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.booking.BookingRepository
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Review
import kotlinx.coroutines.launch

class ClientAppointmentsFragment : Fragment(R.layout.fragment_client_appointments) {

    private val repo = BookingRepository()
    private lateinit var adapter: ClientAppointmentsAdapter
    private lateinit var progress: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvAppointments)
        progress = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        // Configura adapter com o listener de avaliação
        adapter = ClientAppointmentsAdapter { appointment ->
            showRateDialog(appointment)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadData()
    }

    private fun loadData() {
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

    private fun showRateDialog(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rate_service, null)
        
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
                Toast.makeText(requireContext(), "Por favor, selecione uma nota.", Toast.LENGTH_SHORT).show()
            } else {
                submitReview(appointment, rating, comment, dialog)
            }
        }

        dialog.show()
    }

    private fun submitReview(appointment: Appointment, rating: Float, comment: String, dialog: AlertDialog) {
        val review = Review(
            appointmentId = appointment.id,
            providerId = appointment.providerId,
            employeeId = appointment.employeeId,
            clientId = appointment.clientId,
            clientName = appointment.clientName,
            rating = rating,
            comment = comment
        )

        lifecycleScope.launch {
            val success = repo.submitReview(review)
            if (success) {
                Toast.makeText(requireContext(), "Avaliação enviada! Obrigado. ⭐", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadData() // Recarrega a lista para sumir o botão de avaliar
            } else {
                Toast.makeText(requireContext(), "Erro ao enviar avaliação.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}