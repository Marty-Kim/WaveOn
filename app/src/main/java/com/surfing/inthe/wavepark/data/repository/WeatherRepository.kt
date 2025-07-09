package com.surfing.inthe.wavepark.data.repository

import com.surfing.inthe.wavepark.BuildConfig
import com.surfing.inthe.wavepark.data.api.WeatherApiService
import com.surfing.inthe.wavepark.data.database.dao.WeatherDataDao
import com.surfing.inthe.wavepark.data.database.entity.WeatherDataEntity
import com.surfing.inthe.wavepark.data.model.WeatherData
import com.surfing.inthe.wavepark.data.model.WeatherResult
import com.surfing.inthe.wavepark.util.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val weatherDataDao: WeatherDataDao
) {
    private val apiKey: String = ApiConfig.OPENWEATHER_API_KEY

    // RoomDB에서 최신 날씨 데이터 가져오기
    suspend fun getLatestWeatherData(): WeatherData? {
        return weatherDataDao.getLatestWeatherData()?.toDomainModel()
    }

    // RoomDB에서 날씨 데이터 Flow 가져오기
    fun getWeatherDataFromDate(startDate: Date): Flow<List<WeatherData>> {
        return weatherDataDao.getWeatherDataFromDate(startDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

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
            val weatherData = WeatherData(
                weatherStatus = weatherStatus,
                temper = temp.toString(),
                baseDate = baseDate,
                baseTime = baseTime,
                fcstTime = targetFcstTime
            )

            // RoomDB에 날씨 데이터 저장
            saveWeatherData(weatherData)

            WeatherResult.Success(weatherData)
            
        } catch (e: Exception) {
            WeatherResult.Error("API 호출 실패: ${e.message}")
        }
    }

    // RoomDB에 날씨 데이터 저장
    private suspend fun saveWeatherData(weatherData: WeatherData) {
        val entity = weatherData.toEntity()
        weatherDataDao.insertWeatherData(entity)
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

    // Entity를 도메인 모델로 변환하는 확장 함수
    private fun WeatherDataEntity.toDomainModel(): WeatherData {
        return WeatherData(
            weatherStatus = weatherCondition,
            temper = temperature.toString(),
            baseDate = "", // API에서 가져오는 값이므로 빈 문자열
            baseTime = "", // API에서 가져오는 값이므로 빈 문자열
            fcstTime = "" // API에서 가져오는 값이므로 빈 문자열
        )
    }

    // 도메인 모델을 Entity로 변환하는 확장 함수
    private fun WeatherData.toEntity(): WeatherDataEntity {
        return WeatherDataEntity(
            date = Date(),
            temperature = temper.toDoubleOrNull() ?: 0.0,
            humidity = 0, // API에서 제공하지 않는 값
            windSpeed = 0.0, // API에서 제공하지 않는 값
            windDirection = "", // API에서 제공하지 않는 값
            weatherCondition = weatherStatus,
            icon = "" // API에서 제공하지 않는 값
        )
    }
} 