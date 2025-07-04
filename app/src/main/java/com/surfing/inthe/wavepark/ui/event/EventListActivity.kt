package com.surfing.inthe.wavepark.ui.event

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.data.repository.EventRepository
import com.surfing.inthe.wavepark.databinding.ActivityEventListBinding
import com.surfing.inthe.wavepark.ui.home.EventAdapter
import com.surfing.inthe.wavepark.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EventListActivity : AppCompatActivity() {
    private lateinit var eventAdapter: EventAdapter
    private var _binding: ActivityEventListBinding? = null
    private val binding get() = _binding!!
    private val eventListViewModel: EventListViewModel by viewModels()

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
            eventListViewModel.event.collect { events ->
                eventAdapter.submitList(events)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _event = MutableStateFlow<List<Event>>(emptyList())
    val event: StateFlow<List<Event>> = _event.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.getEventsFlow().collect { events ->
                _event.value = events
            }
        }
    }


}
