package com.surfing.inthe.wavepark.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.surfing.inthe.wavepark.data.model.Reservation
import com.surfing.inthe.wavepark.data.model.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val repository: ReservationRepository
) : ViewModel() {
    
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()
    val loading: StateFlow<Boolean> = repository.loading

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            repository.getAllReservations().collect { reservationList ->
                Log.d("LOAD REPOS" , "[예약 데이터] ${reservationList.size}")
                _reservations.emit(reservationList)
            }
        }
    }

    fun addReservations(newList: List<Reservation>) {
        viewModelScope.launch {
            repository.insertReservations(newList)
            loadReservations() // 데이터 다시 로드
        }
    }

    fun setLoading(isLoading: Boolean) = repository.setLoading(isLoading)
    
    fun clear() {
        viewModelScope.launch {
            repository.clearAllReservations()
            _reservations.value = emptyList()
        }
    }

    // WebViewFragment에서 사용하는 메서드
    fun clearReservations() {
        viewModelScope.launch {
            repository.clearAllReservations()
            _reservations.value = emptyList()
        }
    }
} 