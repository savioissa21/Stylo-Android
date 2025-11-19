package com.example.styloandroid.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.R
import com.example.styloandroid.data.ProviderCardData
import com.example.styloandroid.databinding.ItemProviderCardBinding

/**
 * Adapter para exibir os cards de Profissionais/Estabelecimentos na tela do cliente.
 */
class ProviderAdapter(
    private val providers: List<ProviderCardData>,
    private val onScheduleClicked: (ProviderCardData) -> Unit // Callback de clique
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    // 1. ViewHolder: Liga as Views do XML do card aos dados
    inner class ProviderViewHolder(private val binding: ItemProviderCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: ProviderCardData) {
            binding.tvBusinessName.text = provider.businessName
            binding.tvAreaOfWork.text = provider.areaOfWork

            // Formata a avaliação (ex: 4.8 (120))
            binding.tvRating.text =
                itemView.context.getString(R.string.provider_rating, provider.rating, provider.reviewCount)

            // ⭐️ Se você usar o Coil (Coil-kt) ou Glide, o código para carregar a imagem seria:
            /*
            Glide.with(itemView.context)
                .load(provider.profileImageUrl)
                .placeholder(R.drawable.teste) // Ícone de placeholder
                .into(binding.ivProviderLogo)
            */

            // Ação de clique principal
            binding.btnSchedule.setOnClickListener {
                onScheduleClicked(provider)
            }

            // Ação do Favorito
            binding.btnFavorite.setOnClickListener {
                // TODO: Implementar lógica de favoritar
            }
        }
    }


    // 2. Cria o ViewHolder e infla o layout do card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding = ItemProviderCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProviderViewHolder(binding)
    }

    // 3. Retorna o tamanho da lista
    override fun getItemCount() = providers.size

    // 4. Liga o dado à View específica
    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        holder.bind(providers[position])
    }
}