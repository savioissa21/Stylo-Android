package com.example.styloandroid.ui.management

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgendaAdapter(
    private var list: List<Appointment>,
    private val onStatusChange: (String, String) -> Unit // Callback: (ID, NovoStatus)
) : RecyclerView.Adapter<AgendaAdapter.ViewHolder>() {

    fun updateList(newList: List<Appointment>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvClient: TextView = itemView.findViewById(R.id.tvClientName)
        private val tvService: TextView = itemView.findViewById(R.id.tvService)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirm)
        private val btnFinish: Button = itemView.findViewById(R.id.btnFinish)

        fun bind(item: Appointment) {
            // Formatar Data
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvDate.text = sdf.format(Date(item.date))

            tvClient.text = "Cliente: ${item.clientName}"
            tvService.text = item.serviceName
            tvPrice.text = "R$ ${String.format("%.2f", item.price)}"

            // Lógica Visual do Status
            when(item.status) {
                "pending" -> {
                    tvStatus.text = "Pendente"
                    tvStatus.setTextColor(Color.parseColor("#FF9800")) // Laranja
                    btnConfirm.visibility = View.VISIBLE
                    btnFinish.visibility = View.GONE
                }
                "confirmed" -> {
                    tvStatus.text = "Confirmado"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Verde
                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.VISIBLE
                }
                "finished" -> {
                    tvStatus.text = "Concluído"
                    tvStatus.setTextColor(Color.GRAY)
                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.GONE
                }
                else -> {
                    tvStatus.text = item.status
                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.GONE
                }
            }

            // Ações dos Botões
            btnConfirm.setOnClickListener {
                onStatusChange(item.id, "confirmed")
            }

            btnFinish.setOnClickListener {
                onStatusChange(item.id, "finished")
            }
        }
    }
}