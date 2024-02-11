package com.example.weatherwish.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherwish.R
import com.example.weatherwish.ui.airquality.AirQualityViewModel

class AirQualityDataAdapter(
    private val dataList: List<AirQualityViewModel.AirQualityData>,
    private val context: Context
) :
    RecyclerView.Adapter<AirQualityDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_air_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, context)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvValue: TextView = itemView.findViewById(R.id.tv_value)

        fun bind(data: AirQualityViewModel.AirQualityData, context: Context) {
            tvTitle.text = data.title
            tvValue.text = data.value
        }
    }

}