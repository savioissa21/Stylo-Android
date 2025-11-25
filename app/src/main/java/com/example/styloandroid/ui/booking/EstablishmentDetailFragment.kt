package com.example.styloandroid.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentEstablishmentDetailBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class EstablishmentDetailFragment : Fragment(R.layout.fragment_establishment_detail) {

    private var _b: FragmentEstablishmentDetailBinding? = null
    private val b get() = _b!!
    private val vm: BookingViewModel by viewModels()

    private var providerId: String = ""
    private var businessName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentEstablishmentDetailBinding.bind(view)

        // 1. Recebe Argumentos
        arguments?.let {
            providerId = it.getString("providerId") ?: ""
            businessName = it.getString("businessName") ?: "Estabelecimento"

            b.tvBusinessTitle.text = businessName
            // Em breve: b.tvAddress.text = "Endereço vindo do banco..."
        }

        // 2. Configura Toolbar (Botão Voltar)
        b.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 3. Setup RecyclerView
        val adapter = BookingServiceAdapter { service ->
            initiateBookingSequence(service)
        }
        b.rvBookingServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvBookingServices.adapter = adapter

        // 4. Observadores
        vm.services.observe(viewLifecycleOwner) { list ->
            b.progressBar.isVisible = false

            if (list.isEmpty()) {
                b.tvEmpty.isVisible = true
                b.rvBookingServices.isVisible = false
            } else {
                b.tvEmpty.isVisible = false
                b.rvBookingServices.isVisible = true
                adapter.update(list)
            }
        }

        vm.bookingStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                if (msg.contains("sucesso", ignoreCase = true)) {
                    // Feedback chique com SnackBar
                    Snackbar.make(view, "✅ Agendamento Confirmado!", Snackbar.LENGTH_LONG)
                        .setAction("Ver Agenda") {
                            // Opcional: Navegar para "Meus Agendamentos"
                        }
                        .show()

                    // Delay para voltar
                    view.postDelayed({ findNavController().popBackStack() }, 1500)
                } else {
                    Snackbar.make(view, "❌ $msg", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // 5. Carregar Dados
        if (providerId.isNotEmpty()) {
            b.progressBar.isVisible = true
            vm.loadServices(providerId)
        }
    }

    private fun initiateBookingSequence(service: Service) {
        val calendar = Calendar.getInstance()

        // Passo 1: Escolher Data
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            // Passo 2: Escolher Hora
            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                // Passo 3: Confirmar (Via ViewModel)
                confirmBooking(service, calendar.timeInMillis)

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true) // true = 24h format

            timePicker.setTitle("Escolha o Horário")
            timePicker.show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // Bloqueia datas passadas
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.setTitle("Escolha a Data")
        datePicker.show()
    }

    private fun confirmBooking(service: Service, timestamp: Long) {
        // Mostra Loading enquanto salva
        b.progressBar.isVisible = true
        vm.scheduleService(providerId, businessName, service, timestamp)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}