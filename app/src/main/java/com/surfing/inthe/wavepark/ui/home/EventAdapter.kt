package com.surfing.inthe.wavepark.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.surfing.inthe.wavepark.databinding.ItemEventBinding
import javax.inject.Inject

// 데이터 모델
data class EventItem(
    val imageResId: Int? = null, // drawable 리소스 ID (nullable)
    val imageUrl: String? = null, // Firestore 이미지 URL
    val title: String,
    val date: String
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
            binding.imgEventBanner.setImageResource(item.imageResId ?: 0)
            binding.textEventTitle.text = item.title
            binding.textEventDate.text = item.date
        }
    }
    class EventDiffCallback : DiffUtil.ItemCallback<EventItem>() {
        override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
            oldItem.title == newItem.title && oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
            oldItem == newItem
    }
} 