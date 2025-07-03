package com.surfing.inthe.wavepark.ui.event

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.databinding.ActivityEventDetailBinding
import com.surfing.inthe.wavepark.ui.event.EventDetailImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.surfing.inthe.wavepark.data.repository.EventRepository

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    fun loadEvent(eventId: String) {
        // 이벤트를 읽기만 하고, 업데이트/쓰기 작업은 하지 않음
        // Flow에서 해당 이벤트만 찾아서 StateFlow로 노출
        // 여러 번 호출해도 중복 write 없음
        viewModelScope.launch {
            eventRepository.getEventsFlow().collect { events ->
                _event.value = events.find { it.eventId == eventId }
            }
        }
    }
}

@AndroidEntryPoint
class EventDetailActivity : AppCompatActivity() {
    private var _binding: ActivityEventDetailBinding? = null
    private val binding get() = _binding!!
    private val eventDetailViewModel: EventDetailViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val eventId = intent.getStringExtra("idx") ?: ""
        eventDetailViewModel.loadEvent(eventId)
        observeEventDetail()
    }
    private fun observeEventDetail() {
        lifecycleScope.launch {
            eventDetailViewModel.event.collect { event ->
                event?.let {
                    binding.eventTitle.text = it.title
                    binding.eventDate.text = it.startDate.toString()
                    binding.imageList.adapter = EventDetailImageAdapter(it.imageList)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}