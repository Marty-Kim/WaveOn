package com.surfing.inthe.wavepark.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.data.model.TemperatureData
import com.surfing.inthe.wavepark.data.model.TemperatureResult
import com.surfing.inthe.wavepark.data.repository.TemperatureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemperatureViewModel @Inject constructor(
    private val temperatureRepository: TemperatureRepository
) : ViewModel() {
    
    private val _temperatureData = MutableLiveData<TemperatureData>()
    val temperatureData: LiveData<TemperatureData> = _temperatureData
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    fun fetchTemperatureData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            // 수온 데이터만 가져오기
            when (val tempResult = temperatureRepository.fetchTemperatureData()) {
                is TemperatureResult.Success -> {
                    _temperatureData.value = tempResult.data
                }
                is TemperatureResult.Error -> {
                    _errorMessage.value = tempResult.message
                }
            }
            
            _isLoading.value = false
        }
    }
    
    // 표시용 값 포맷팅 (null 체크 포함)
    fun getDisplayValue(value: Double?, unit: String = ""): String {
        return if (value != null) "${value}${unit}" else "-"
    }
    
    // 날짜 포맷팅 (MM/DD 형식)
    fun getFormattedDate(): String {
        val today = java.util.Calendar.getInstance()
        val month = String.format("%02d", today.get(java.util.Calendar.MONTH) + 1)
        val day = String.format("%02d", today.get(java.util.Calendar.DAY_OF_MONTH))
        return "$month/$day"
    }
} 