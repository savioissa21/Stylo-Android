package com.example.styloandroid.ui.client.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.styloandroid.R
import com.example.styloandroid.data.model.ProviderCardData

class ProviderAdapter(
    private var providers: List<ProviderCardData>,
    private val onClick: (ProviderCardData) -> Unit,
    private val onFavoriteClick: (ProviderCardData) -> Unit // NOVO CALLBACK
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
        holder.bind(providers[position])
    }

    override fun getItemCount() = providers.size

    inner class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvBusinessName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProviderImage)
        private val btnBook: Button = itemView.findViewById(R.id.btnBookNow)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite) // Pega o botão

        fun bind(provider: ProviderCardData) {
            tvName.text = provider.businessName
            tvCategory.text = provider.areaOfWork
            tvRating.text = provider.rating.toString()

            // Define o ícone correto
            val heartIcon = if (provider.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            btnFavorite.setImageResource(heartIcon)

            // Carrega imagem (mantido igual)
            if (provider.profileImageUrl != null) {
                ivImage.load(provider.profileImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                }
            } else {
                ivImage.setImageResource(R.drawable.ic_launcher_background)
            }

            // Cliques
            itemView.setOnClickListener { onClick(provider) }
            btnBook.setOnClickListener { onClick(provider) }

            // Clique do Favorito
            btnFavorite.setOnClickListener {
                onFavoriteClick(provider)
            }
        }
    }
}