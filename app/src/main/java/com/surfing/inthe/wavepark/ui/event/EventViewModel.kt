package com.surfing.inthe.wavepark.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.ui.home.EventItem
import com.surfing.inthe.wavepark.ui.home.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _events = MutableLiveData<List<EventItem>>()
    val events: LiveData<List<EventItem>> = _events


    init {
        viewModelScope.launch {
            _events.value = eventRepository.getEventsFromFirestore()
        }

    }


}