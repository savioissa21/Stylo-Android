package com.example.styloandroid.data.management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service
import java.text.NumberFormat
import java.util.Locale

class ServiceAdapter(
    private var list: List<Service> = emptyList(),
    private val onDeleteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    fun updateList(newList: List<Service>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_service, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])
    override fun getItemCount() = list.size

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val tvDuration: TextView = v.findViewById(R.id.tvDuration)
        val tvTeamCount: TextView = v.findViewById(R.id.tvTeamCount)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)

        fun bind(item: Service) {
            tvName.text = item.name
            
            val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvPrice.text = format.format(item.price)
            
            tvDuration.text = "• ${item.durationMin} min"
            
            val count = item.employeeIds.size
            tvTeamCount.text = if (count == 0) "Nenhum profissional" else "$count profissional(is)"
            
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}