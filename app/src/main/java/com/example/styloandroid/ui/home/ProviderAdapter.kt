package com.example.styloandroid.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.ProviderCardData

class ProviderAdapter(
    private var providers: List<ProviderCardData>,
    private val onClick: (ProviderCardData) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    fun updateList(newList: List<ProviderCardData>) {
        providers = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_card, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.bind(provider)
    }

    override fun getItemCount() = providers.size

    inner class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvBusinessName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val btnBook: Button = itemView.findViewById(R.id.btnBookNow)

        fun bind(provider: ProviderCardData) {
            tvName.text = provider.businessName
            tvCategory.text = provider.areaOfWork
            tvRating.text = provider.rating.toString()

            // Clique tanto no card quanto no botão levam ao detalhe
            itemView.setOnClickListener { onClick(provider) }
            btnBook.setOnClickListener { onClick(provider) }
        }
    }
}