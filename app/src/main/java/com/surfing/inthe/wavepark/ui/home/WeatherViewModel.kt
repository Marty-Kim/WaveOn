package com.surfing.inthe.wavepark.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.data.model.*
import com.surfing.inthe.wavepark.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    fun fetchWeatherData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            println("Weather launched")

            when (val result = weatherRepository.fetchWeatherData()) {
                is WeatherResult.Success -> {
                    _weatherData.value = result.data
                    println("Weather success ${result.data}")
                }
                is WeatherResult.Error -> {
                    _errorMessage.value = result.message
                    println("Weather Error ${result.message}")
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 날씨 상태에 따른 아이콘 리소스 반환
     */
    fun getWeatherIconRes(weatherStatus: String): Int {
        return when (weatherStatus) {
            "맑음" -> R.drawable.sunny
            "구름많음" -> R.drawable.suncloudy
            "흐림" -> R.drawable.overcast_cloud
            "비" -> R.drawable.heavy_rain
            "눈" -> R.drawable.snow_cloud
            "비/눈" -> R.drawable.cloud_sun_rain_snow
            "빗방울" -> R.drawable.rain_day_sun
            "빗방울눈날림" -> R.drawable.cloud_sun_rain_snow
            "눈날림" -> R.drawable.snow_cloud
            "강수없음" -> R.drawable.sunny
            else -> R.drawable.sunny // 기본값
        }
    }
} 