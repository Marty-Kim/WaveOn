package com.surfing.inthe.wavepark.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.R
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

    private val _allSessions = MutableLiveData<Map<String, List<DailySession>>>()
    val allSessions: LiveData<Map<String, List<DailySession>>> = _allSessions

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _selectedSessions = MutableLiveData<List<DailySession>>()
    val selectedSessions: LiveData<List<DailySession>> = _selectedSessions

    private val _lastRefreshTime = MutableLiveData<String>()
    val lastRefreshTime: LiveData<String> = _lastRefreshTime

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    data class DailySessionPair(
        val lesson: DailySession?,
        val normal: DailySession?
    )

    private val _selectedSessionPairs = MutableLiveData<List<DailySessionPair>>()
    val selectedSessionPairs: LiveData<List<DailySessionPair>> = _selectedSessionPairs

    init {
        refreshSessions()
    }

    fun refreshSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val sessions = withContext(Dispatchers.IO) {
                    dashboardRepository.getFutureDailySessions(limitDays = 14)
                }
                _allSessions.value = sessions
                _lastRefreshTime.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                // 기본 선택 날짜(오늘)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                setSelectedDate(today)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        val sessions = _allSessions.value?.get(date) ?: emptyList()
        // time, waves가 같은 세션끼리 merge
        val lessonMap = sessions.filter { it.islesson }.associateBy { it.time to it.waves }
        val normalMap = sessions.filter { !it.islesson }.associateBy { it.time to it.waves }
        val allKeys = (lessonMap.keys + normalMap.keys).distinct().sortedBy { it.first }
        val merged = allKeys.map { key ->
            DailySessionPair(
                lesson = lessonMap[key],
                normal = normalMap[key]
            )
        }
        _selectedSessionPairs.value = merged
    }

    fun refreshData() {
        _selectedDate.value?.let { date ->
            // 구버전 예약 관련 LiveData 및 함수 완전 제거
        }
    }

}