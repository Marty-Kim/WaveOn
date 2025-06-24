package com.surfing.inthe.wavepark.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Home 화면의 ViewModel (MVVM)
 * Repository를 DI로 주입받아 데이터를 LiveData로 노출.
 * UI는 ViewModel만 관찰, 데이터 소스와 분리.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _events = MutableLiveData<List<EventItem>>()
    val events: LiveData<List<EventItem>> = _events

    init {
        // Repository에서 데이터 받아와 LiveData에 저장
        _events.value = eventRepository.getEvents()
    }
}