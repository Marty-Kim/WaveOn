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
    val imageResId: Int, // drawable 리소스 ID
    val title: String,
    val date: String
)

/**
 * MVVM의 Repository 역할. (Home 화면)
 * 데이터 소스(API, DB, 샘플 등)와 ViewModel 사이의 추상화 계층.
 */
interface EventRepository {
    fun getEvents(): List<EventItem>
}

/**
 * 실제 데이터 제공 구현체. (샘플)
 * @Inject 생성자: Hilt가 DI로 주입할 수 있게 함.
 */
class EventRepositoryImpl @Inject constructor() : EventRepository {
    override fun getEvents(): List<EventItem> {
        return listOf(
            EventItem(
                imageResId = com.surfing.inthe.wavepark.R.drawable.ic_launcher_background,
                title = "서핑 페스티벌 2024",
                date = "2024.06.01 ~ 2024.06.30"
            ),
            EventItem(
                imageResId = com.surfing.inthe.wavepark.R.drawable.ic_launcher_background,
                title = "여름 할인 이벤트",
                date = "2024.07.01 ~ 2024.07.15"
            ),
            EventItem(
                imageResId = com.surfing.inthe.wavepark.R.drawable.ic_launcher_background,
                title = "신규 회원 웰컴!",
                date = "상시 진행"
            )
        )
    }
}

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
            binding.imgEventBanner.setImageResource(item.imageResId)
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