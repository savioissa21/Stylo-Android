package com.example.styloandroid.ui.manager.agenda

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Appointment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgendaAdapter(
    private var list: List<Appointment>,
    private val onStatusChange: (String, String) -> Unit
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
        private val tvDateFull: TextView = itemView.findViewById(R.id.tvDateFull) 

        private val tvClient: TextView = itemView.findViewById(R.id.tvClientName)
        private val tvService: TextView = itemView.findViewById(R.id.tvService)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirm)
        private val btnFinish: Button = itemView.findViewById(R.id.btnFinish)

        fun bind(item: Appointment) {
            val dateObj = Date(item.date)

            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvDate.text = sdfTime.format(dateObj)

            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvDateFull.text = sdfDate.format(dateObj)

            tvClient.text = item.clientName
            tvService.text = item.serviceName

            val formatPrice = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvPrice.text = formatPrice.format(item.price)

            when(item.status) {
                "pending" -> {
                    tvStatus.text = "Pendente"
                    tvStatus.setTextColor(Color.parseColor("#FF9800")) 
                    tvStatus.setBackgroundResource(R.drawable.ic_launcher_background)
                    tvStatus.background?.setTint(Color.parseColor("#FFF3E0"))

                    btnConfirm.visibility = View.VISIBLE
                    btnFinish.visibility = View.GONE
                }
                "confirmed" -> {
                    tvStatus.text = "Confirmado"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50")) 
                    tvStatus.setBackgroundResource(R.drawable.ic_launcher_background)
                    tvStatus.background?.setTint(Color.parseColor("#E8F5E9"))

                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.VISIBLE
                }
                "finished" -> {
                    tvStatus.text = "Concluído"
                    tvStatus.setTextColor(Color.GRAY)
                    tvStatus.background = null 

                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.GONE
                }
                else -> {
                    tvStatus.text = item.status
                    btnConfirm.visibility = View.GONE
                    btnFinish.visibility = View.GONE
                }
            }

            btnConfirm.setOnClickListener {
                onStatusChange(item.id, "confirmed")
            }

            btnFinish.setOnClickListener {
                onStatusChange(item.id, "finished")
            }
        }
    }
}