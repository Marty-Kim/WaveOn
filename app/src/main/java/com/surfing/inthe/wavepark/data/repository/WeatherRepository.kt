package com.surfing.inthe.wavepark.data.repository

import com.surfing.inthe.wavepark.BuildConfig
import com.surfing.inthe.wavepark.data.api.WeatherApiService
import com.surfing.inthe.wavepark.data.model.WeatherData
import com.surfing.inthe.wavepark.data.model.WeatherResult
import com.surfing.inthe.wavepark.util.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
) {
    private val apiKey: String  = ApiConfig.OPENWEATHER_API_KEY

    suspend fun fetchWeatherData(): WeatherResult = withContext(Dispatchers.IO) {
        try {
            val now = Calendar.getInstance()
            val baseDate = getBaseDate(now)
            val baseTime = getNearestTime(now)
            val targetFcstTime = getFcstTime(now)

            val response = weatherApiService.getUltraSrtFcst(
                serviceKey = apiKey,
                baseDate = baseDate,
                baseTime = baseTime
            )
            println("Weather response ${response.toString()}")

            val items = response.response.body.items.item

            // SKY와 PTY 항목 찾기
            val sky = items.find {
                it.category == "SKY" && it.fcstTime == targetFcstTime
            }?.fcstValue

            val pty = items.find {
                it.category == "PTY" && it.fcstTime == targetFcstTime
            }?.fcstValue
            val temp = items.find {
                it.category == "T1H" && it.fcstTime == targetFcstTime
            }?.fcstValue

            val weatherStatus = parseWeatherStatus(sky, pty)
            WeatherResult.Success(
                WeatherData(
                    weatherStatus = weatherStatus,
                    temper = temp.toString(),
                    baseDate = baseDate,
                    baseTime = baseTime,
                    fcstTime = targetFcstTime
                )
            )
//            WeatherResult.Success(
//                    WeatherData(
//                        weatherStatus = "weatherStatus",
//                        temper = "temper",
//                        baseDate = "baseDate",
//                        baseTime = "baseTime",
//                        fcstTime = "targetFcstTime"
//                    )
//                    )
            
        } catch (e: Exception) {
            WeatherResult.Error("API 호출 실패: ${e.message}")
        }
    }
    
    private fun getBaseDate(now: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(now.time)
    }
    
    private fun getNearestTime(now: Calendar): String {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        
        return if (minute < 30) {
            String.format("%02d30", hour - 1)
        } else {
            String.format("%02d30", hour)
        }
    }
    
    private fun getFcstTime(now: Calendar): String {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        
        return if (minute < 30) {
            String.format("%02d00", hour)
        } else {
            String.format("%02d00", hour + 1)
        }
    }
    
    private fun parseWeatherStatus(sky: String?, pty: String?): String {
        // PTY (강수형태) 처리
        if (pty != null && pty != "0") {
            return when (pty) {
                "1" -> "비"
                "2" -> "비" // 비/눈
                "3" -> "눈"
                "5" -> "비" // 빗방울
                "6" -> "비" // 빗방울눈날림
                "7" -> "눈" // 눈날림
                else -> "강수없음"
            }
        }
        
        // SKY (하늘상태) 처리
        return when (sky) {
            "1" -> "맑음"
            "3" -> "구름많음"
            "4" -> "흐림"
            else -> "알수없음"
        }
    }
} 