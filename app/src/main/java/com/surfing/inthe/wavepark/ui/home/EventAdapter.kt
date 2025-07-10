package com.surfing.inthe.wavepark.ui.home

import android.content.Intent
import android.content.Intent.ACTION_VIEW
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
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.ui.event.EventDetailActivity
import javax.inject.Inject
import androidx.core.net.toUri

class EventAdapter : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Event) {
            Log.d("EventAdapter", "Loading image URL: ${item.imageUrl}")
            Glide.with(binding.imgEventBanner).load(item.imageUrl).into(binding.imgEventBanner);
            binding.textEventTitle.text = item.title
            binding.textEventDate.text = item.startDate.toString() // TODO: 날짜 포맷팅 필요
            
            binding.root.setOnClickListener {
                Log.d("EventAdapter", "Loading imageList: ${item.imageList}")
                item.eventUrl?.let {
                    binding.root.context.startActivity(Intent(ACTION_VIEW,
                        "https://www.wavepark.co.kr/board/event?act=view/detail/${item.eventId}".toUri()))
//                    val intent = Intent(binding.root.context, EventDetailActivity::class.java)
//                    intent.putExtra("title", item.title)
//                    intent.putExtra("date", item.startDate.toString())
//                    intent.putExtra("idx", item.eventId)
//                    intent.putExtra("webUrl", item.eventUrl)
//                    intent.putStringArrayListExtra("images", ArrayList(item.imageList))
//                    binding.root.context.startActivity(intent)
                }
            }
        }
    }
    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.eventId == newItem.eventId
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
} 