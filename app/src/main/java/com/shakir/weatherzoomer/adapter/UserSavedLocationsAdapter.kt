package com.shakir.weatherzoomer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shakir.weatherzoomer.databinding.ItemSavedLocationBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.model.UserLocationItem
import com.shakir.weatherzoomer.utils.Utils

class UserSavedLocationsAdapter(private val locations: ArrayList<UserLocationItem>,
    private val onItemInteractionListener: OnItemInteractionListener) :
    RecyclerView.Adapter<UserSavedLocationsAdapter.DrawerItemViewHolder>() {

    inner class DrawerItemViewHolder(private val binding: ItemSavedLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, locationObject: UserLocationItem) {
            binding.tvLocation.text = locationObject.userLocationModel.location
            if (locationObject.userLocationModel.currentLocation) {
                binding.tvIsCurrentLocation.visibility = View.VISIBLE
            }
            binding.imgDeleteSavedLocation.setSafeOnClickListener {
                onItemInteractionListener.onItemDeleted(index, locationObject.locationId)
            }
            binding.cdParentLayout.setSafeOnClickListener {
                onItemInteractionListener.onItemSelectedListener(locationObject.userLocationModel.location)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerItemViewHolder {
        val binding = ItemSavedLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrawerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrawerItemViewHolder, position: Int) {
        val item = locations[position]
        holder.bind(position, item)
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    fun deleteItem(index: Int) {
        locations.removeAt(index)
        notifyItemRemoved(index)
    }

    interface OnItemInteractionListener {
        fun onItemDeleted(position: Int, locationId: String)
        fun onItemSelectedListener(location: String)
    }

}
