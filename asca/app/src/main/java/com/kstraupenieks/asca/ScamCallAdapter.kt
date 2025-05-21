package com.kstraupenieks.asca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScamCallAdapter(private val scamCalls: List<ScamCall>) :
    RecyclerView.Adapter<ScamCallAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberText = itemView.findViewById<TextView>(R.id.textNumber)
        val dateText = itemView.findViewById<TextView>(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scam_call, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = scamCalls.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scamCall = scamCalls[position]
        holder.numberText.text = scamCall.number
        holder.dateText.text = java.text.DateFormat.getDateTimeInstance().format(scamCall.date)
    }
}