package com.robin729.aqi.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.robin729.aqi.R
import com.robin729.aqi.databinding.RowLayoutFavouritesBinding
import com.robin729.aqi.data.model.favouritesAqi.Data
import com.robin729.aqi.utils.Constants
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
            binding.apply {
                location.text = item.locName
                aqiValue.text = "AQI: ${item.index.details.aqi}"
                category.text = item.index.details.category
                icFace.setBackgroundColor(Color.parseColor(item.index.details.color))
                icFace.setImageResource(Util.getFaceBasedOnAqi(item.index.details.aqi))
                otherSideFaceColor.setBackgroundColor(Color.parseColor(item.index.details.color))
                headCard.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putParcelable(Constants.FAV_LAT_LNG, item.latLng)
                    val navOption = NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
                    root.findNavController().navigate(R.id.mainFragment, bundle, navOption)
                }
            }

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