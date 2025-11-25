package com.example.styloandroid.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentEstablishmentDetailBinding
import java.util.Calendar

class EstablishmentDetailFragment : Fragment(R.layout.fragment_establishment_detail) {
    private var _b: FragmentEstablishmentDetailBinding? = null
    private val b get() = _b!!
    private val vm: BookingViewModel by viewModels()

    // Variáveis recebidas da tela anterior
    private var providerId: String = ""
    private var businessName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentEstablishmentDetailBinding.bind(view)

        // 1. Recebe os argumentos (Passados via Bundle)
        arguments?.let {
            providerId = it.getString("providerId") ?: ""
            businessName = it.getString("businessName") ?: "Estabelecimento"
            b.tvBusinessTitle.text = businessName
        }

        // 2. Configura Adapter
        val adapter = BookingServiceAdapter { service ->
            showDateTimePicker(service)
        }
        b.rvBookingServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvBookingServices.adapter = adapter

        // 3. Observa
        vm.services.observe(viewLifecycleOwner) { list ->
            if(list.isEmpty()) Toast.makeText(requireContext(), "Nenhum serviço disponível", Toast.LENGTH_SHORT).show()
            adapter.update(list)
        }

        vm.bookingStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                if (msg.contains("sucesso")) parentFragmentManager.popBackStack() // Volta pra home
            }
        }

        // 4. Carrega
        if (providerId.isNotEmpty()) {
            vm.loadServices(providerId)
        }
    }

    private fun showDateTimePicker(service: Service) {
        val calendar = Calendar.getInstance()

        // Date Picker
        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            // Time Picker (dentro do callback da data)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                // Confirma Agendamento
                vm.scheduleService(providerId, businessName, service, calendar.timeInMillis)

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}