package com.shakir.weatherzoomer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakir.weatherzoomer.R

class DrawerItemAdapter(private val itemList: List<String>) :
    RecyclerView.Adapter<DrawerItemAdapter.DrawerItemViewHolder>() {

    inner class DrawerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.drawer_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drawer_item, parent, false)
        return DrawerItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawerItemViewHolder, position: Int) {
        holder.textView.text = itemList[position]
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
