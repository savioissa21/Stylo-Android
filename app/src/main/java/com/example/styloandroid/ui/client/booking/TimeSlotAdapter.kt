package com.example.styloandroid.ui.client.booking

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeSlotAdapter(
    private val onSlotSelected: (Long) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    private var slots: List<Long> = emptyList()
    private var selectedPosition: Int = -1

    fun submitList(newSlots: List<Long>) {
        slots = newSlots
        selectedPosition = -1 // Reseta seleção ao mudar a lista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(slots[position], position)
    }

    override fun getItemCount() = slots.size

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardTimeSlot)
        private val tvTime: TextView = itemView.findViewById(R.id.tvSlotTime)

        fun bind(timeInMillis: Long, position: Int) {
            // Formata hora (ex: 14:30)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(timeInMillis))

            // Estilo de Seleção
            if (position == selectedPosition) {
                // SELECIONADO: Fundo Verde e Texto Branco
                card.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                tvTime.setTextColor(Color.WHITE)
                card.strokeColor = Color.TRANSPARENT
            } else {
                // NÃO SELECIONADO: Fundo Branco e Texto Preto
                card.setCardBackgroundColor(Color.WHITE)
                tvTime.setTextColor(Color.BLACK)
                card.strokeColor = Color.parseColor("#E0E0E0")
            }

            itemView.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition

                // Evita crash se a posição for inválida
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                if (selectedPosition != RecyclerView.NO_POSITION) notifyItemChanged(selectedPosition)

                onSlotSelected(timeInMillis)
            }
        }
    }
}