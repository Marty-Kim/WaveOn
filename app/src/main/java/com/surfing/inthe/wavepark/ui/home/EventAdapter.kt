package com.surfing.inthe.wavepark.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surfing.inthe.wavepark.databinding.ItemEventBinding
import javax.inject.Inject
// 데이터 모델
data class EventItem(
    val event_url: String? = null, // drawable 리소스 ID (nullable)
    val date: String? = null, // drawable 리소스 ID (nullable)
    val imageUrl: String? = null, // Firestore 이미지 URL
    val crawled_at: String? = null, // Firestore 이미지 URL
    val d_day: Int? = null, // Firestore 이미지 URL
    val event_id: String? = null, // Firestore 이미지 URL
    val event_type: String? = null, // Firestore 이미지 URL
    val title: String,
)

class EventAdapter : ListAdapter<EventItem, EventAdapter.EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventItem) {
            Glide.with(binding.root.context).load(item.imageUrl).into(binding.imgEventBanner);
            binding.textEventTitle.text = item.title
            binding.textEventDate.text = item.date
            binding.root.setOnClickListener {
                item.event_url.let {
                    val url = it

                }
            }
        }
    }
    class EventDiffCallback : DiffUtil.ItemCallback<EventItem>() {
        override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
            oldItem.title == newItem.title && oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
            oldItem == newItem
    }
} 