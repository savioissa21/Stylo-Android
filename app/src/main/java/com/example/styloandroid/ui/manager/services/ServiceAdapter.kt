package com.example.styloandroid.ui.manager.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.model.Service
import java.text.NumberFormat
import java.util.Locale

class ServiceAdapter(
    private var list: List<Service> = emptyList(),
    private var isReadOnly: Boolean = false, // NOVO PARÂMETRO
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    fun updateList(newList: List<Service>) {
        list = newList
        notifyDataSetChanged()
    }

    // Método para atualizar o modo de leitura
    fun setReadOnly(readOnly: Boolean) {
        this.isReadOnly = readOnly
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

            // --- LÓGICA DE VISUALIZAÇÃO ---
            if (isReadOnly) {
                // Se for funcionário, esconde a lixeira e desabilita clique de edição
                btnDelete.isVisible = false
                itemView.isClickable = false
                // Opcional: remover o background ripple para parecer estático
                itemView.background = null
            } else {
                // Se for Gestor, tudo normal
                btnDelete.isVisible = true
                itemView.isClickable = true
                itemView.setOnClickListener { onEditClick(item) }
                btnDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }
}