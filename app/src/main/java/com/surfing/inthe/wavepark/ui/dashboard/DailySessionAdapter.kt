package com.surfing.inthe.wavepark.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.databinding.ItemReservationBinding

class DailySessionAdapter : RecyclerView.Adapter<DailySessionAdapter.ViewHolder>() {
    private var items: List<DashboardViewModel.DailySessionPair> = emptyList()

    fun submitList(list: List<DashboardViewModel.DailySessionPair>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // 두 번째 바인딩(아래쪽)도 생성
        val binding2 = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, binding2)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(
        private val binding: ItemReservationBinding,
        private val binding2: ItemReservationBinding
    ) : RecyclerView.ViewHolder(
        LinearLayout(binding.root.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            addView(binding.root, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            addView(binding2.root, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    ) {
        fun bind(pair: DashboardViewModel.DailySessionPair) {
            // lesson 세션 바인딩 (위)
            if (pair.lesson != null) {
                bindSession(binding, pair.lesson, isLesson = true)
                binding.root.visibility = View.VISIBLE
            } else {
                binding.root.visibility = View.GONE
            }
            // normal 세션 바인딩 (아래)
            if (pair.normal != null) {
                bindSession(binding2, pair.normal, isLesson = false)
                binding2.root.visibility = View.VISIBLE
            } else {
                binding2.root.visibility = View.GONE
            }
        }

        private fun bindSession(binding: ItemReservationBinding, session: DailySession, isLesson: Boolean) {
            val cardView = binding.cardContainer as? MaterialCardView
            val context = binding.root.context
            val startTime = session.time.takeLast(8).substring(0, 5)
            val endTime = try {
                val hour = startTime.substring(0, 2).toInt()
                val nextHour = (hour + (if (session.isfunding || session.isNight) 2 else 1)) % 24
                String.format("%02d:%s", nextHour, startTime.substring(3, 5))
            } catch (e: Exception) {
                "--:--"
            }
            binding.textTimeRange.text = "$startTime~$endTime"
            binding.textSession.text = "${session.name} (${session.waves})"
            binding.textLeftCove.visibility = VISIBLE
            binding.textLeftSeat.visibility = VISIBLE
            if (isLesson) {
                binding.textLeftCove.text = "좌"
                binding.textLeftSeat.text = session.left.toString()
                binding.textRightCove.text = "우"
                binding.textRightSeat.text = "-"
                cardView?.strokeColor = ContextCompat.getColor(context, R.color.session_blue)
            } else if (session.isfunding || session.isNight){
//                binding.textLeftCove.text = "펀딩률"
//                val rate = session.left.split("|")[0]
//                val fundingRate = (session.left.toDouble() / session.waves.toDouble() * 100).toInt()
//                binding.textLeftSeat.text = session.left.toString()
                binding.textLeftCove.visibility = GONE
                binding.textLeftSeat.visibility = GONE
                binding.textRightCove.text = "잔여"
                binding.textRightSeat.text = session.right.toString()
            }else {
                binding.textLeftCove.text = "좌"
                binding.textLeftSeat.text = session.left.toString()
                binding.textRightCove.text = "우"
                binding.textRightSeat.text = session.right.toString()

            }
            val strokeColor = when (session.name) {
                "초급" -> ContextCompat.getColor(context, R.color.session_blue)
                "중급" -> ContextCompat.getColor(context, R.color.session_yellow)
                "상급" -> ContextCompat.getColor(context, R.color.session_red)
                else -> ContextCompat.getColor(context, R.color.session_blue)
            }
            cardView?.setCardBackgroundColor(strokeColor)
            cardView?.strokeColor = strokeColor
            cardView?.setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_surface))
            cardView?.strokeWidth = 4
        }
    }
} 