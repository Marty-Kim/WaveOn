package com.surfing.inthe.wavepark.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.data.api.ReservationData
import com.surfing.inthe.wavepark.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Dashboard 화면의 ViewModel (MVVM)
 * Repository를 DI로 주입받아 데이터를 LiveData로 노출.
 * UI는 ViewModel만 관찰, 데이터 소스와 분리.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _reservationData = MutableLiveData<List<ReservationData>>()
    val reservationData: LiveData<List<ReservationData>> = _reservationData

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        // 기본값으로 오늘 날짜 설정
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _selectedDate.value = today
        loadReservationData(today)
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadReservationData(date)
    }

    fun loadReservationData(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkModule.reservationApiService.getReservations(date)
                }
                _reservationData.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        _selectedDate.value?.let { date ->
            loadReservationData(date)
        }
    }
}