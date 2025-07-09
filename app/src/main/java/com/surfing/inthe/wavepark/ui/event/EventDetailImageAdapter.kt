package com.surfing.inthe.wavepark.ui.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surfing.inthe.wavepark.databinding.ItemEventImageBinding

class EventDetailImageAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<EventDetailImageAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemEventImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }
    override fun getItemCount(): Int = imageUrls.size

    class ImageViewHolder(private val binding: ItemEventImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Glide.with(binding.rootImageview).load(imageUrl).into(binding.rootImageview)
        }
    }
} 