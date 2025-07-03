package com.surfing.inthe.wavepark.ui.event

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.databinding.ActivityEventListBinding
import com.surfing.inthe.wavepark.ui.home.EventAdapter
import com.surfing.inthe.wavepark.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventListActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
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
    private fun observeViewModel() {
        lifecycleScope.launch {
            homeViewModel.events.collect { events ->
                eventAdapter.submitList(events)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}