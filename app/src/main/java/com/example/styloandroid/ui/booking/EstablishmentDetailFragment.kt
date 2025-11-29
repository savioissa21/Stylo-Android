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
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentEstablishmentDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EstablishmentDetailFragment : Fragment(R.layout.fragment_establishment_detail) {

    private var _b: FragmentEstablishmentDetailBinding? = null
    private val b get() = _b!!
    private val vm: BookingViewModel by viewModels()

    private var providerId: String = ""
    private var businessName: String = ""
    private var teamList: List<AppUser> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentEstablishmentDetailBinding.bind(view)

        // Recebe dados da navegação anterior
        arguments?.let {
            providerId = it.getString("providerId") ?: ""
            businessName = it.getString("businessName") ?: "Estabelecimento"
            b.tvBusinessTitle.text = businessName
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        // Carrega dados iniciais
        if (providerId.isNotEmpty()) {
            b.progressBar.isVisible = true
            vm.loadServices(providerId)
            vm.loadTeam(providerId)
            vm.loadReviews(providerId)
        }
    }

    private fun setupToolbar() {
        b.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        val adapter = BookingServiceAdapter { service ->
            openBookingSheet(service)
        }
        b.rvBookingServices.layoutManager = LinearLayoutManager(requireContext())
        b.rvBookingServices.adapter = adapter
    }

    private fun setupObservers() {
        vm.services.observe(viewLifecycleOwner) { list ->
            b.progressBar.isVisible = false
            b.tvEmpty.isVisible = list.isEmpty()
            b.rvBookingServices.isVisible = list.isNotEmpty()
            (b.rvBookingServices.adapter as BookingServiceAdapter).update(list)
        }

        vm.team.observe(viewLifecycleOwner) { teamList = it }

        vm.bookingStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
                if (msg.contains("sucesso", true)) {
                    // Volta para a tela anterior após sucesso
                    findNavController().popBackStack()
                }
            }
        }
    }

    // --- BOTTOM SHEET (Onde os IDs são usados) ---
    private fun openBookingSheet(service: Service) {
        // Infla o layout que criamos no Passo 1
        val view = layoutInflater.inflate(R.layout.bottom_sheet_booking, null)
        val sheet = BottomSheetDialog(requireContext())
        sheet.setContentView(view)

        // Agora os IDs vão funcionar porque o XML existe
        val tvService = view.findViewById<android.widget.TextView>(R.id.tvServiceNameSheet)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupEmployees)
        val btnDate = view.findViewById<View>(R.id.btnPickTime)
        val tvDate = view.findViewById<android.widget.TextView>(R.id.tvSelectedDate)
        val btnConfirm = view.findViewById<android.widget.Button>(R.id.btnConfirmBooking)

        tvService.text = "${service.name} - R$ ${String.format("%.2f", service.price)}"

        // 1. Configura Chips dos Funcionários
        val qualifiedEmployees = teamList.filter { emp ->
            service.employeeIds.isEmpty() || 
            service.employeeIds.contains(emp.uid) || 
            service.employeeIds.contains(emp.email)
        }

        var selectedEmployee: AppUser? = null
        
        if (qualifiedEmployees.isEmpty()) {
            Toast.makeText(requireContext(), "Sem profissionais disponíveis.", Toast.LENGTH_SHORT).show()
            return 
        }

        qualifiedEmployees.forEachIndexed { index, emp ->
            val chip = Chip(requireContext())
            chip.text = emp.name
            chip.isCheckable = true
            chip.id = index 
            chipGroup.addView(chip)
            
            if (index == 0) {
                chip.isChecked = true
                selectedEmployee = emp
            }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) selectedEmployee = qualifiedEmployees[checkedId]
        }

        // 2. Configura Data/Hora
        var selectedTimestamp: Long = 0L
        val calendar = Calendar.getInstance()

        btnDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                
                TimePickerDialog(requireContext(), { _, hour, minute ->
                    // Validação simples de horário comercial (9h as 18h)
                    if (hour < 9 || hour >= 20) {
                        Toast.makeText(requireContext(), "Horário fechado! Escolha entre 09:00 e 20:00", Toast.LENGTH_LONG).show()
                        return@TimePickerDialog
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    
                    selectedTimestamp = calendar.timeInMillis
                    val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    tvDate.text = sdf.format(calendar.time)
                    
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 3. Botão Confirmar
        btnConfirm.setOnClickListener {
            if (selectedEmployee == null) {
                Toast.makeText(requireContext(), "Selecione um profissional", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedTimestamp == 0L) {
                Toast.makeText(requireContext(), "Selecione data e hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val appointment = Appointment(
                serviceId = service.id,
                serviceName = service.name,
                price = service.price,
                durationMin = service.durationMin,
                date = selectedTimestamp,
                status = "pending",
                providerId = this.providerId,
                employeeId = selectedEmployee!!.uid,
                employeeName = selectedEmployee!!.name
            )
            
            vm.createAppointment(appointment)
            sheet.dismiss()
        }

        sheet.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}