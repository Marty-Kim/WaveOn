package com.surfing.inthe.wavepark.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.data.model.Event
import com.surfing.inthe.wavepark.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Home 화면의 ViewModel (MVVM)
 * Repository를 DI로 주입받아 데이터를 StateFlow로 노출.
 * UI는 ViewModel만 관찰, 데이터 소스와 분리.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    init {
        viewModelScope.launch {
            println("Home Viewmodel init")
            _isLoading.value = true
            // 1. Firestore/Room 동기화
            eventRepository.syncEventsIfNeeded()
            // 2. Room Flow collect (초기화 시 1회만)
            launch {
                eventRepository.getEventsFlow()
                    .take(1) // 또는 .first()
                    .collect { events ->
                    _events.emit(events)
                }
            }
            // 3. 이미지 파싱 (최초 1회만)
            launch {
                eventRepository.fetchAndSaveEventImagesIfNeeded()
            }
            _isLoading.value = false
        }
    }


    // 수동 새로고침
    fun refreshEvents() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // 1. Firestore/Room 동기화
            eventRepository.syncEventsIfNeeded()
            // 2. 이미지 파싱
            eventRepository.fetchAndSaveEventImagesIfNeeded()
            _isRefreshing.value = false
        }
    }

    fun getEventById(eventId: String): Event? {
        return _events.value.find { it.eventId == eventId }
    }
}