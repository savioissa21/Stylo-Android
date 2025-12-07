package com.example.styloandroid.ui.booking

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styloandroid.R
import com.example.styloandroid.data.auth.AppUser
import com.example.styloandroid.data.model.Appointment
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.FragmentEstablishmentDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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

        arguments?.let {
            providerId = it.getString("providerId") ?: ""
            businessName = it.getString("businessName") ?: "Estabelecimento"
            b.tvBusinessTitle.text = businessName
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()

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

        vm.ratingStats.observe(viewLifecycleOwner) { (rating, count) ->
            val formatted = String.format(Locale("pt", "BR"), "%.1f (%d avaliações)", rating, count)
            b.tvRatingDetail.text = formatted
        }

        // --- MUDANÇA AQUI: Toast no lugar de Snackbar ---
        vm.bookingStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                // Exibe o Toast
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                // Se deu certo, volta para a tela anterior
                if (msg.contains("sucesso", true)) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    // --- LÓGICA DO BOTTOM SHEET ---
    private fun openBookingSheet(service: Service) {
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_booking, null)
        val sheetDialog = BottomSheetDialog(requireContext())
        sheetDialog.setContentView(sheetView)

        // IMPORTANTE: Como mudamos o XML para NestedScrollView,
        // certifique-se de que os IDs abaixo batem com o novo XML corrigido.
        val tvService = sheetView.findViewById<android.widget.TextView>(R.id.tvServiceNameSheet)
        val chipGroup = sheetView.findViewById<ChipGroup>(R.id.chipGroupEmployees)
        val btnDate = sheetView.findViewById<View>(R.id.btnPickDate)
        val tvDateString = sheetView.findViewById<android.widget.TextView>(R.id.tvSelectedDateString)
        val rvSlots = sheetView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTimeSlots)
        val progressSlots = sheetView.findViewById<View>(R.id.progressSlots)
        val tvNoSlots = sheetView.findViewById<android.widget.TextView>(R.id.tvNoSlots)
        val btnConfirm = sheetView.findViewById<android.widget.Button>(R.id.btnConfirmBooking)

        tvService.text = "${service.name} - R$ ${String.format("%.2f", service.price)}"

        var selectedEmployee: AppUser? = null
        val selectedDateCal = Calendar.getInstance()
        var selectedTimestamp: Long = 0L

        val timeAdapter = TimeSlotAdapter { timestamp ->
            selectedTimestamp = timestamp
            btnConfirm.isEnabled = true
            btnConfirm.alpha = 1.0f
            btnConfirm.text = "Confirmar para " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }
        rvSlots.layoutManager = GridLayoutManager(requireContext(), 4)
        rvSlots.adapter = timeAdapter

        fun refreshSlots() {
            if (selectedEmployee == null) return

            if (!vm.isEstablishmentOpenOn(selectedDateCal)) {
                tvNoSlots.text = "Fechado neste dia da semana."
                tvNoSlots.isVisible = true
                rvSlots.isVisible = false
                btnConfirm.isEnabled = false
                btnConfirm.alpha = 0.5f
                return
            } else {
                tvNoSlots.text = "Nenhum horário disponível."
            }

            selectedTimestamp = 0L
            btnConfirm.isEnabled = false
            btnConfirm.alpha = 0.5f
            btnConfirm.text = "Selecione um horário"

            vm.loadTimeSlots(selectedDateCal, service.durationMin, selectedEmployee!!.uid)
        }

        vm.availableSlots.observe(viewLifecycleOwner) { slots ->
            if (sheetDialog.isShowing) {
                timeAdapter.submitList(slots)
                tvNoSlots.isVisible = slots.isEmpty()
                rvSlots.isVisible = slots.isNotEmpty()
            }
        }

        vm.isLoadingSlots.observe(viewLifecycleOwner) { loading ->
            if (sheetDialog.isShowing) {
                progressSlots.isVisible = loading
                if(loading) {
                    rvSlots.isVisible = false
                    tvNoSlots.isVisible = false
                }
            }
        }

        val qualifiedEmployees = teamList.filter { emp ->
            service.employeeIds.isEmpty() ||
                    service.employeeIds.contains(emp.uid) ||
                    service.employeeIds.contains(emp.email)
        }

        if (qualifiedEmployees.isEmpty()) {
            Toast.makeText(requireContext(), "Sem profissionais disponíveis.", Toast.LENGTH_SHORT).show()
            return
        }

        qualifiedEmployees.forEachIndexed { index, emp ->
            val chip = Chip(requireContext())
            chip.text = emp.name
            chip.isCheckable = true

            // CORREÇÃO 1: Usar generateViewId() para evitar ID 0 ou conflitos
            chip.id = View.generateViewId()
            chip.tag = index // Guardamos o índice na TAG para recuperar depois

            chipGroup.addView(chip)

            if (index == 0) {
                chip.isChecked = true
                selectedEmployee = emp
            }
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != View.NO_ID) {
                // Recupera o chip pelo ID gerado
                val chip = group.findViewById<Chip>(checkedId)
                if (chip != null) {
                    val index = chip.tag as Int // Recupera o índice da tag
                    selectedEmployee = qualifiedEmployees[index]
                    refreshSlots()
                }
            }
        }

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        tvDateString.text = sdfDate.format(selectedDateCal.time)

        refreshSlots()

        btnDate.setOnClickListener {
            // Verifica se o fragmento ainda está válido
            val context = context ?: return@setOnClickListener

            val datePicker = DatePickerDialog(
                context, // Usa o context seguro
                { _, year, month, day ->

                    if (!isAdded) return@DatePickerDialog

                    val tempCal = Calendar.getInstance()
                    tempCal.set(year, month, day)

                    if (!vm.isEstablishmentOpenOn(tempCal)) {
                        Toast.makeText(requireContext(), "O estabelecimento não abre neste dia.", Toast.LENGTH_LONG).show()
                    } else {
                        selectedDateCal.set(year, month, day)
                        tvDateString.text = sdfDate.format(selectedDateCal.time)
                        refreshSlots()
                    }
                },
                selectedDateCal.get(Calendar.YEAR),
                selectedDateCal.get(Calendar.MONTH),
                selectedDateCal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        btnConfirm.setOnClickListener {
            if (selectedEmployee == null || selectedTimestamp == 0L) return@setOnClickListener

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

            // Inicia o agendamento
            vm.createAppointment(appointment)

            // Fecha o modal imediatamente (o Toast aparecerá em seguida pelo observer)
            sheetDialog.dismiss()
        }

        sheetDialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}