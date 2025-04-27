package com.refun.owner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.refun.owner.R
import com.refun.owner.databinding.ItemHistoryBinding
import com.refun.owner.model.HistoryItem

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: HistoryItem) {
            binding.apply {
                timestampTextView.text = item.timestamp
                pointsTextView.text = "${item.points} Points"
                statusIndicator.setBackgroundResource(
                    if (item.status) R.color.success_green else R.color.error_red
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
} 