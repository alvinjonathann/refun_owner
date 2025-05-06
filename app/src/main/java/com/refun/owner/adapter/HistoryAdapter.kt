package com.refun.owner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.refun.owner.R
import com.refun.owner.databinding.ItemHistoryBinding
import com.refun.owner.model.HistoryItem
import androidx.core.graphics.toColorInt

class HistoryAdapter(private val items: List<HistoryItem>)       {
//    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

//    class ViewHolder(itemView: ItemHistoryBinding) : RecyclerView.ViewHolder(itemView) {
//
//        fun bind(item: HistoryItem) {
//            tanggalQR.text = item.timestamp
//            statusQR.text = item.status.toString()
////            timestampTextView.text = item.timestamp
////            pointsTextView.text = "${item.points} Points"
//            statusQR.setTextColor(
//                if (item.status) R.color.success_green.toString().toColorInt() else R.color.error_red.toString().toColorInt()
//            )
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemHistoryBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position])
//    }
//
//    override fun getItemCount() = items.size
}