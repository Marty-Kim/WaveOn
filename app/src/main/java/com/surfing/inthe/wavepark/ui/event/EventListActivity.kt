package com.surfing.inthe.wavepark.ui.event

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.databinding.ActivityEventListBinding
import com.surfing.inthe.wavepark.ui.home.EventAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventListActivity : AppCompatActivity() {
    // Hilt로 ViewModel 주입 (by viewModels())
    private val homeViewModel: EventViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter
    private var _binding: ActivityEventListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupEventRecyclerView()
        observeViewModel()
    }
    // RecyclerView 초기화
    private fun setupEventRecyclerView() {
        eventAdapter = EventAdapter()
        binding.recyclerViewEvents.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = eventAdapter
        }

    }
    // ViewModel의 LiveData를 관찰하여 UI 업데이트
    private fun observeViewModel() {
        homeViewModel.events.observe(this) { events ->
            events.forEach {
                println("events ${it.title}" )
            }
            
            // 필터링된 이벤트만 표시
            val filteredEvents = events.filter { event ->
                event.title.contains("서핑") ||
                event.title.contains("surfing", ignoreCase = true) ||
                event.title.contains("night", ignoreCase = true) ||
                event.title.contains("surf pass", ignoreCase = true) ||
                event.title.contains("펀딩") ||
                event.title.contains("레슨")
            }
            
            println("Filtered events count: ${filteredEvents.size}")
            eventAdapter.submitList(filteredEvents)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}