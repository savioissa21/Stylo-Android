package com.example.styloandroid.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styloandroid.data.model.Service
import com.example.styloandroid.databinding.ItemBookingServiceBinding

class BookingServiceAdapter(
    private var list: List<Service> = emptyList(),
    private val onBookClick: (Service) -> Unit
) : RecyclerView.Adapter<BookingServiceAdapter.Holder>() {

    inner class Holder(val b: ItemBookingServiceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemBookingServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]
        holder.b.tvServiceName.text = item.name
        holder.b.tvServicePrice.text = "R$ ${String.format("%.2f", item.price)} • ${item.durationMin} min"
        holder.b.btnBook.setOnClickListener { onBookClick(item) }
    }

    override fun getItemCount() = list.size

    fun update(newList: List<Service>) {
        list = newList
        notifyDataSetChanged()
    }
}