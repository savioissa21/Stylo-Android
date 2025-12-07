package com.example.styloandroid.ui.client.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service
import java.text.NumberFormat
import java.util.Locale

class BookingServiceAdapter(
    private var services: List<Service> = emptyList(),
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<BookingServiceAdapter.ViewHolder>() {

    fun update(newList: List<Service>) {
        services = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvServiceDuration)

        fun bind(service: Service) {
            tvName.text = service.name

            val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvPrice.text = format.format(service.price)

            tvDuration.text = "${service.durationMin} min"

            itemView.setOnClickListener {
                onServiceClick(service)
            }
        }
    }
}