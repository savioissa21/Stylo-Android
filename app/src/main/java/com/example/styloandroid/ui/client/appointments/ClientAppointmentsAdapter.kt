package com.example.styloandroid.ui.client.appointments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClientAppointmentsAdapter(
    private var list: List<Appointment> = emptyList(),
    private val onRateClick: (Appointment) -> Unit = {},
    private val onCancelClick: (Appointment) -> Unit = {} 
) : RecyclerView.Adapter<ClientAppointmentsAdapter.ViewHolder>() {

    fun updateList(newList: List<Appointment>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        private val tvBusiness: TextView = itemView.findViewById(R.id.tvBusinessName)
        private val tvService: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusBadge)

        private val btnRate: Button = itemView.findViewById(R.id.btnRate)
        private val btnCancel: Button = itemView.findViewById(R.id.btnCancel)

        fun bind(item: Appointment) {
            val cal = Calendar.getInstance().apply { timeInMillis = item.date }
            tvDay.text = cal.get(Calendar.DAY_OF_MONTH).toString()
            tvMonth.text = SimpleDateFormat("MMM", Locale("pt", "BR")).format(cal.time).uppercase()

            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdfTime.format(cal.time)

            tvBusiness.text = item.businessName
            val serviceText = if (item.employeeName.isNotEmpty())
                "${item.serviceName} com ${item.employeeName}"
            else
                item.serviceName
            tvService.text = serviceText

            when (item.status) {
                "pending" -> {
                    tvStatus.text = "PENDENTE"
                    tvStatus.setTextColor(Color.parseColor("#EF6C00"))
                    tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))

                    btnCancel.isVisible = true
                    btnRate.isVisible = false
                }
                "confirmed" -> {
                    tvStatus.text = "CONFIRMADO"
                    tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                    tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))

                    btnCancel.isVisible = true
                    btnRate.isVisible = false
                }
                "finished" -> {
                    tvStatus.text = "CONCLUÍDO"
                    tvStatus.setTextColor(Color.GRAY)
                    tvStatus.setBackgroundColor(Color.parseColor("#F5F5F5"))

                    btnCancel.isVisible = false
                    btnRate.isVisible = !item.hasReview
                }
                "canceled" -> {
                    tvStatus.text = "CANCELADO"
                    tvStatus.setTextColor(Color.RED)
                    tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))

                    btnCancel.isVisible = false
                    btnRate.isVisible = false
                }
            }

            btnRate.setOnClickListener { onRateClick(item) }

            btnCancel.setOnClickListener { onCancelClick(item) }
        }
    }
}