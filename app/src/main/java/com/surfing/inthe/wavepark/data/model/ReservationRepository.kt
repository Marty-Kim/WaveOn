package com.surfing.inthe.wavepark.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor() {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun addReservations(newList: List<Reservation>) {
        _reservations.value = _reservations.value + newList
        if (newList.isNotEmpty()) _loading.value = false
    }

    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    fun clear() {
        _reservations.value = emptyList()
        _loading.value = true
    }
} 