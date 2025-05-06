package com.refun.owner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.refun.owner.R
import com.refun.owner.model.HistoryItem

class HistoryItemAdapter(var mList: MutableList<HistoryItem>) : RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder>() {
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tanggalQR)
        val tvStatus: TextView = itemView.findViewById(R.id.statusQR)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.qr_history_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.tvTanggal.text = mList[position].timestamp
        holder.tvStatus.text = mList[position].status.toString()
        holder.tvStatus.setTextColor(
            if (mList[position].status) R.color.success_green.toString().toColorInt() else R.color.error_red.toString().toColorInt()
        )
    }
}