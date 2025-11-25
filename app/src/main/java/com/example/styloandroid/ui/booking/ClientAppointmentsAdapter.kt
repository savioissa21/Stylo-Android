package com.example.styloandroid.ui.booking

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClientAppointmentsAdapter(
    private var list: List<Appointment> = emptyList()
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

        fun bind(item: Appointment) {
            // Formatar Data (Dia e Mês separados)
            val cal = Calendar.getInstance().apply { timeInMillis = item.date }
            tvDay.text = cal.get(Calendar.DAY_OF_MONTH).toString()
            tvMonth.text = SimpleDateFormat("MMM", Locale("pt", "BR")).format(cal.time).uppercase()

            // Formatar Hora
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdfTime.format(cal.time)

            tvBusiness.text = item.businessName
            tvService.text = item.serviceName

            // Status Visual
            when (item.status) {
                "pending" -> {
                    tvStatus.text = "PENDENTE"
                    tvStatus.setTextColor(Color.parseColor("#EF6C00")) // Laranja Escuro
                    tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0")) // Laranja Claro
                }
                "confirmed" -> {
                    tvStatus.text = "CONFIRMADO"
                    tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Verde Escuro
                    tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9")) // Verde Claro
                }
                "finished" -> {
                    tvStatus.text = "CONCLUÍDO"
                    tvStatus.setTextColor(Color.GRAY)
                    tvStatus.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }
                "canceled" -> {
                    tvStatus.text = "CANCELADO"
                    tvStatus.setTextColor(Color.RED)
                    tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                }
            }
        }
    }
}