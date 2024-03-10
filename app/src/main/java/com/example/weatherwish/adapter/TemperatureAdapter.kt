package com.example.weatherwish.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.weatherwish.R
import com.example.weatherwish.constants.SystemOfMeasurement
import com.example.weatherwish.model.Hour
import com.example.weatherwish.utils.Utils

class TemperatureAdapter(private val dataList: List<Hour>, private val context: Context, private val systemOfMeasurement: SystemOfMeasurement) : RecyclerView.Adapter<TemperatureAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_temperature, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, context, systemOfMeasurement)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTemperature: TextView = itemView.findViewById(R.id.tv_temperature)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_icon)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvHumidity: TextView = itemView.findViewById(R.id.tv_humidity)

        fun bind(data: Hour, context: Context, systemOfMeasurement: SystemOfMeasurement) {
            tvTime.text = Utils.convertToHourTime(data.time_epoch.toLong())
            val temperature = if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                "${data.temp_c.toInt()}°C"
            } else {
               "${data.temp_f.toInt()}°F"
            }
            tvTemperature.text = temperature
            imgIcon.setImageResource(context.resources.getIdentifier(Utils.generateStringFromUrl(data.condition.icon), "drawable", context.packageName))
            tvHumidity.text = "${data.humidity}%"
        }
    }

}