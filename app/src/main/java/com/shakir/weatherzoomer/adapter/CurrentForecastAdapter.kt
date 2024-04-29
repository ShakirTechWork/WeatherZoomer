package com.shakir.weatherzoomer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.model.Forecastday
import com.shakir.weatherzoomer.utils.Utils

class CurrentForecastAdapter(private val dataList: List<Forecastday>, private val context: Context) : RecyclerView.Adapter<CurrentForecastAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, context)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_icon)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val tvHighestTemperature: TextView = itemView.findViewById(R.id.tv_highest_temperature)
        val tvLowestTemperature: TextView = itemView.findViewById(R.id.tv_lowest_temperature)

        fun bind(data: Forecastday, context: Context) {
            tvDate.text = Utils.convertUnixTimeToDate(data.date_epoch.toLong())
            tvDay.text = Utils.convertUnixTimeToDayName(data.date_epoch.toLong())
            imgIcon.setImageResource(context.resources.getIdentifier(Utils.generateStringFromUrl(data.day.condition.icon), "drawable", context.packageName))
            tvStatus.text = data.day.condition.text
            tvHighestTemperature.text = "${data.day.maxtemp_c.toInt()}°C"
            tvLowestTemperature.text = "${data.day.mintemp_c.toInt()}°C"
        }
    }
}