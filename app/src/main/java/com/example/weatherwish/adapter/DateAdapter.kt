package com.example.weatherwish.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherwish.R
import com.example.weatherwish.model.Forecastday
import com.example.weatherwish.utils.Utils

class DateAdapter(private val dataList: List<Forecastday>,
                  private val onItemSelectedListener: OnItemSelectedListener) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_layout, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, position, onItemSelectedListener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date_time)

        fun bind(
            data: Forecastday,
            position: Int,
            onItemSelectedListener: OnItemSelectedListener
        ) {
            val text = when (position) {
                0 -> {
                    "Today"
                }
                1 -> {
                    "${Utils.convertUnixTimeToFormattedDayAndDate(data.date_epoch.toLong())} (Tommorrow)"
                }
                else -> {
                    Utils.convertUnixTimeToFormattedDayAndDate(data.date_epoch.toLong())
                }
            }
            tvDate.text = text
            tvDate.setOnClickListener {
                onItemSelectedListener.onItemSelected(position)
            }
        }
    }

    interface OnItemSelectedListener {
        fun onItemSelected(index: Int)
    }

}