package com.surfing.inthe.wavepark.ui.event

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.ui.home.EventItem

class EventDetailImageAdapter(private val imageUrls: EventItem) : RecyclerView.Adapter<EventDetailImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.rootImageview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = imageUrls
        Glide.with(holder.imageView.context)
            .load(item.imageList[position])
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            if(position == item.imageList.size - 1){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.event_url))
                holder.imageView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = imageUrls.imageList.size
} 