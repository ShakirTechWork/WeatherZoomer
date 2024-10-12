package com.shakir.weatherzoomer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shakir.weatherzoomer.databinding.ItemLocationResultBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.model.searchLocation.SearchLocationResultModel
import com.shakir.weatherzoomer.model.searchLocation.SearchLocationResultModelItem
import com.shakir.weatherzoomer.utils.Utils

class LocationSearchResultsAdapter(private val dataList: SearchLocationResultModel, private val onLocationSelectedListener: OnLocationSelectedListener):
    RecyclerView.Adapter<LocationSearchResultsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onLocationSelectedListener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        Utils.printDebugLog("item: ${item.name}")
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: ItemLocationResultBinding,
        private val onLocationSelectedListener: OnLocationSelectedListener
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(locationObject: SearchLocationResultModelItem) {
            val location = "${locationObject.name}, ${locationObject.region}, ${locationObject.country}"
            binding.tvLocation.text = location

            binding.cvParentLayout.setSafeOnClickListener {
                onLocationSelectedListener.onLocationSelected(location)
            }
        }

    }

    interface OnLocationSelectedListener {
        fun onLocationSelected(location: String)
    }

}