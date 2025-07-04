package com.surfing.inthe.wavepark.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
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


