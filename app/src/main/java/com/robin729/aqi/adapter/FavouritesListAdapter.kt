package com.robin729.aqi.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.robin729.aqi.databinding.RowLayoutFavouritesBinding
import com.robin729.aqi.model.favouritesAqi.Data
import com.robin729.aqi.utils.Util


class FavouritesListAdapter :
    ListAdapter<Data, FavouritesListAdapter.ViewHolder>(DataDiffCallbacks()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouritesListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RowLayoutFavouritesBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouritesListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: RowLayoutFavouritesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Data) {
            binding.location.text = item.locName
            binding.aqiValue.text = "AQI: ${item.index.details.aqi}"
            binding.category.text = item.index.details.category
            binding.icFace.setBackgroundColor(Color.parseColor(item.index.details.color))
            binding.icFace.setImageResource(Util.getFaceBasedOnAqi(item.index.details.aqi))
            binding.otherSideFaceColor.setBackgroundColor(Color.parseColor(item.index.details.color))
        }
    }

    class DataDiffCallbacks : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }
}