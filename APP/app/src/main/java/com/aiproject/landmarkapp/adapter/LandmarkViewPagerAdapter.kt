package com.aiproject.landmarkapp.adapter

import android.location.Location
import android.location.LocationManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aiproject.landmarkapp.R
import com.aiproject.landmarkapp.retrofit.LandmarkModel

class LandmarkViewPagerAdapter(val itemClicked: (LandmarkModel) -> Unit) :
    ListAdapter<LandmarkModel, LandmarkViewPagerAdapter.ItemViewHolder>(differ) {
    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(landmarkModel: LandmarkModel) {
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val distanceTextView = view.findViewById<TextView>(R.id.distanceTextView)
            val addressTextView = view.findViewById<TextView>(R.id.addressTextView)
            val telTextView = view.findViewById<TextView>(R.id.telTextView)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)

            titleTextView.text = landmarkModel.title
            distanceTextView.text = landmarkModel.distance
            addressTextView.text = landmarkModel.address
            telTextView.text = landmarkModel.tel

            view.setOnClickListener {
                itemClicked(landmarkModel)
            }

            Glide.with(thumbnailImageView.context)
                .load(landmarkModel.image)
                .into(thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(
            inflater.inflate(
                R.layout.item_landmark_detail_for_viewpager,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<LandmarkModel>() {
            override fun areItemsTheSame(oldItem: LandmarkModel, newItem: LandmarkModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LandmarkModel, newItem: LandmarkModel): Boolean {
                return oldItem == newItem
            }

        }
    }


}