package com.example.styloandroid.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Appointment
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
    
    // Lista temporária da equipe carregada
    private var teamList: List<AppUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentEstablishmentDetailBinding.bind(view)

        arguments?.let {
            providerId = it.getString("providerId") ?: ""
            businessName = it.getString("businessName") ?: "Estabelecimento"
            b.tvBusinessTitle.text = businessName
        }

        b.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // Adapter: Ao clicar no serviço, inicia o fluxo
        val adapter = BookingServiceAdapter { service ->
            showEmployeeSelector(service)
        }
        b.rvBookingServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvBookingServices.adapter = adapter

        // Observadores
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
        
        // Guarda a equipe na memória quando carregar
        vm.team.observe(viewLifecycleOwner) { team ->
            teamList = team
        }

        vm.bookingStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                if (msg.contains("confirmado", ignoreCase = true)) {
                    Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
                    view.postDelayed({ findNavController().popBackStack() }, 1500)
                } else {
                    Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Carrega dados
        if (providerId.isNotEmpty()) {
            b.progressBar.isVisible = true
            vm.loadServices(providerId)
            vm.loadTeam(providerId) // Carrega a equipe em paralelo
        }
    }

    /**
     * Passo 1: Selecionar Profissional
     */
// Em EstablishmentDetailFragment.kt

    private fun showEmployeeSelector(service: Service) {
        if (teamList.isEmpty()) {
            Snackbar.make(requireView(), "Carregando profissionais...", Snackbar.LENGTH_SHORT).show()
            vm.loadTeam(providerId)
            return
        }

        // CORREÇÃO: Filtra apenas quem tem o ID na lista do serviço
        // Se a lista do serviço estiver vazia, assumimos que o Dono (providerId) faz, ou todos fazem.
        val qualifiedEmployees = teamList.filter { employee ->
            service.employeeIds.isEmpty() || service.employeeIds.contains(employee.uid) || service.employeeIds.contains(employee.email)
        }

        if (qualifiedEmployees.isEmpty()) {
            Snackbar.make(requireView(), "Nenhum profissional disponível para este serviço.", Snackbar.LENGTH_LONG).show()
            return
        }

        val names = qualifiedEmployees.map { it.name }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Escolha o Profissional")
            .setItems(names) { _, which ->
                val selectedEmployee = qualifiedEmployees[which]
                showDateTimePicker(service, selectedEmployee)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDateTimePicker(service: Service, employee: AppUser) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->

                // Validação de horário comercial
                if (hour < 9 || hour >= 18) {
                    com.google.android.material.snackbar.Snackbar.make(requireView(), "Atendimento apenas das 09h às 18h.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                    return@TimePickerDialog
                }

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val selectedTime = calendar.timeInMillis

                // 👇 CRIAÇÃO DO AGENDAMENTO CORRIGIDA 👇
                val appointment = Appointment(
                    serviceId = service.id,
                    serviceName = service.name,
                    price = service.price,
                    durationMin = service.durationMin,
                    date = selectedTime,
                    status = "pending",

                    // CORREÇÃO AQUI: Usamos a variável 'providerId' do Fragmento, não do serviço
                    providerId = this.providerId,

                    // Dados do funcionário selecionado
                    employeeId = employee.uid,
                    employeeName = employee.name
                )

                // Chama o ViewModel
                vm.createAppointment(appointment)

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            timePicker.setTitle("Horário com ${employee.name}")
            timePicker.show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }



    override fun onDestroyView() { super.onDestroyView(); _b = null }
}


