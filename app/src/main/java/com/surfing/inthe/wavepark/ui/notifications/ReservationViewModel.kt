package com.surfing.inthe.wavepark.ui.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.surfing.inthe.wavepark.data.model.Reservation
import com.surfing.inthe.wavepark.data.model.ReservationRepository
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val repository: ReservationRepository
) : ViewModel() {
    val reservations: StateFlow<List<Reservation>> = repository.reservations
    val loading: StateFlow<Boolean> = repository.loading

    fun addReservations(newList: List<Reservation>) = repository.addReservations(newList)
    fun setLoading(isLoading: Boolean) = repository.setLoading(isLoading)
    fun clear() = repository.clear()
} 