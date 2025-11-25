package com.example.styloandroid.ui.management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.databinding.ItemServiceBinding
import com.example.styloandroid.data.model.Service

class ServiceAdapter(
    private var list: List<Service> = emptyList(),
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.Holder>() {

    inner class Holder(val b: ItemServiceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]
        holder.b.tvServiceName.text = item.name
        holder.b.tvServiceDetails.text = "${item.durationMin} min • R$ ${String.format("%.2f", item.price)}"

        holder.b.btnDelete.setOnClickListener { onDeleteClick(item.id) }
    }

    override fun getItemCount() = list.size

    fun update(newList: List<Service>) {
        list = newList
        notifyDataSetChanged()
    }
}