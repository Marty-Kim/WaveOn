package com.surfing.inthe.wavepark.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.data.api.ReservationData
import com.surfing.inthe.wavepark.databinding.ItemReservationBinding

class ReservationAdapter : ListAdapter<ReservationData, ReservationAdapter.ReservationViewHolder>(ReservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReservationViewHolder(private val binding: ItemReservationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reservation: ReservationData) {
            // 시간 범위 추출 (예: 10:00~11:00)
            val startTime = reservation.시간.takeLast(8).substring(0, 5)
            val endTime = try {
                // 다음 세션의 시간 또는 +1시간 계산 (여기선 단순히 +1시간 처리)
                val hour = startTime.substring(0, 2).toInt()
                val nextHour = (hour + 1) % 24
                String.format("%02d:%s", nextHour, startTime.substring(3, 5))
            } catch (e: Exception) {
                "--:--"
            }
            binding.textTimeRange.text = "$startTime~$endTime"

            // 세션명
            binding.textSession.text = reservation.세션

            // 카드 테두리 색상 세션별 적용 (MaterialCardView)
            val cardView = binding.cardContainer as? MaterialCardView
            val context = binding.root.context
            val strokeColor = when (reservation.세션) {
                "초급" -> ContextCompat.getColor(context, R.color.session_blue)
                "중급" -> ContextCompat.getColor(context, R.color.session_yellow)
                "상급" -> ContextCompat.getColor(context, R.color.session_red)
                else -> ContextCompat.getColor(context, R.color.gray_light)
            }
            cardView?.setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_surface))
            cardView?.strokeColor = strokeColor
            cardView?.strokeWidth = 4

            // 좌/우 코브 및 잔여좌석
            if (reservation.방향 == "좌") {
                binding.textLeftCove.text = "좌"
                binding.textLeftSeat.text = reservation.남은좌석.toString()
                binding.textRightCove.text = "우"
                binding.textRightSeat.text = "-"
                setSeatColor(binding, left = reservation.남은좌석, right = null)
            } else if (reservation.방향 == "우") {
                binding.textLeftCove.text = "좌"
                binding.textLeftSeat.text = "-"
                binding.textRightCove.text = "우"
                binding.textRightSeat.text = reservation.남은좌석.toString()
                setSeatColor(binding, left = null, right = reservation.남은좌석)
            } else {
                // 혹시 모를 예외 처리
                binding.textLeftCove.text = "좌"
                binding.textLeftSeat.text = "-"
                binding.textRightCove.text = "우"
                binding.textRightSeat.text = "-"
                setSeatColor(binding, left = null, right = null)
            }
        }

        private fun setSeatColor(
            binding: ItemReservationBinding,
            left: Int?,
            right: Int?
        ) {
            val context = binding.root.context
            // 좌
            if (left != null) {
                binding.textLeftSeat.text = left.toString()
                binding.textLeftSeat.setTextColor(
                    if (left == 0) ContextCompat.getColor(context, R.color.coral_orange)
                    else ContextCompat.getColor(context, R.color.wave_blue)
                )
            } else {
                binding.textLeftSeat.text = "-"
                binding.textLeftSeat.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
            }
            // 우
            if (right != null) {
                binding.textRightSeat.text = right.toString()
                binding.textRightSeat.setTextColor(
                    if (right == 0) ContextCompat.getColor(context, R.color.coral_orange)
                    else ContextCompat.getColor(context, R.color.wave_blue)
                )
            } else {
                binding.textRightSeat.text = "-"
                binding.textRightSeat.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
            }
        }
    }

    private class ReservationDiffCallback : DiffUtil.ItemCallback<ReservationData>() {
        override fun areItemsTheSame(oldItem: ReservationData, newItem: ReservationData): Boolean {
            return oldItem.시간 == newItem.시간 &&
                   oldItem.세션 == newItem.세션 &&
                   oldItem.방향 == newItem.방향
        }
        override fun areContentsTheSame(oldItem: ReservationData, newItem: ReservationData): Boolean {
            return oldItem == newItem
        }
    }
} 