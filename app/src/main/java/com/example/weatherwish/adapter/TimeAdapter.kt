package com.example.weatherwish.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherwish.R
import com.example.weatherwish.model.SelectedTimeModel
import com.example.weatherwish.utils.Utils

class TimeAdapter(private val dataList: List<SelectedTimeModel>,
                  private val context: Context,
    private val onTimeItemClickListener: OnTimeItemClickListener
):
    RecyclerView.Adapter<TimeAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_layout, parent, false)
        return TimeAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: TimeAdapter.ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, context, onTimeItemClickListener, position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvRemoveTime: TextView = itemView.findViewById(R.id.tv_remove_time)

        fun bind(
            data: SelectedTimeModel,
            context: Context,
            onTimeItemClickListener: OnTimeItemClickListener,
            position: Int
        ) {
            tvTime.text = data.readable_time
            tvRemoveTime.setOnClickListener {
                onTimeItemClickListener.onDeleteItem(position)
            }
        }
    }


    interface OnTimeItemClickListener {
        fun onDeleteItem(index: Int)
    }


}