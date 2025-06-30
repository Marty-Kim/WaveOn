package com.surfing.inthe.wavepark.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.surfing.inthe.wavepark.BuildConfig

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

    private val _weatherInfo = MutableLiveData<WeatherInfo>()
    val weatherInfo: LiveData<WeatherInfo> = _weatherInfo

    init {
        // Firestore에서 이벤트 목록 가져오기
        // 실제 날씨 정보 fetch
        viewModelScope.launch {
            _events.value = eventRepository.getEventsFromFirestore()
//            fetchWeatherInfo()
        }

    }

//    private suspend fun fetchWeatherInfo() {
//        // 시흥시 정왕동 위도/경도 (예시: 37.3402, 126.7335)
//        val lat = 37.3402
//        val lon = 126.7335
//        val apiKey = BuildConfig.OPENWEATHER_API_KEY
//        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=kr"
//        val client = OkHttpClient()
//        val request = Request.Builder().url(url).build()
//        val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
//        if (response.isSuccessful) {
//            response.body?.string()?.let { body ->
//                val json = JSONObject(body)
//                val weatherArray = json.getJSONArray("weather")
//                val weatherObj = weatherArray.getJSONObject(0)
//                val main = json.getJSONObject("main")
//                val desc = weatherObj.getString("description")
//                val temp = main.getDouble("temp")
//                val icon = weatherObj.getString("icon")
//                val iconRes = when {
//                    icon.startsWith("01") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_sunny
//                    icon.startsWith("02") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_cloudy
//                    icon.startsWith("03") || icon.startsWith("04") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_cloud
//                    icon.startsWith("09") || icon.startsWith("10") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_rainy
//                    icon.startsWith("11") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_thunder
//                    icon.startsWith("13") -> com.surfing.inthe.wavepark.R.drawable.ic_weather_snow
//                    else -> com.surfing.inthe.wavepark.R.drawable.ic_weather_sunny
//                }
//                _weatherInfo.postValue(WeatherInfo(
//                    iconRes = iconRes,
//                    desc = "${desc.replaceFirstChar { it.uppercase() }}, ${temp.toInt()}°C",
//                    location = "경기도 시흥시 정왕동"
//                ))
//            }
//        }
//    }

    data class WeatherInfo(
        val iconRes: Int,
        val desc: String,
        val location: String
    )
}