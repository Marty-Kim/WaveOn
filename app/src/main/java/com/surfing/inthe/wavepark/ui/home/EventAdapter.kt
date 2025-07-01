package com.surfing.inthe.wavepark.ui.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surfing.inthe.wavepark.GlideApp
import com.surfing.inthe.wavepark.databinding.ItemEventBinding
import com.surfing.inthe.wavepark.ui.event.EventDetailActivity
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
    val imageList : List<String> = emptyList<String>()
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
//            GlideApp.with(binding.imgEventBanner).load(item.imageUrl).into(binding.imgEventBanner)
//            GlideApp.with(binding.imgEventBanner)
//                .load(item.imageUrl)
//                .into(binding.imgEventBanner)]
            Log.d("EventAdapter", "Loading image URL: ${item.imageUrl}")
            Glide.with(binding.imgEventBanner).load(item.imageUrl).into(binding.imgEventBanner);
            binding.textEventTitle.text = item.title
            binding.textEventDate.text = item.date
            binding.root.setOnClickListener {
                item.event_url.let {
                    val intent = Intent(binding.root.context, EventDetailActivity::class.java)
                    intent.putExtra("title", item.title)
                    intent.putExtra("date", item.date)
                    intent.putExtra("idx", item.event_id)
                    intent.putExtra("webUrl", item.event_url)
                    intent.putStringArrayListExtra("images", ArrayList(item.imageList))
                    binding.root.context.startActivity(intent)
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