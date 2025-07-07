package com.surfing.inthe.wavepark.shared.api

import com.surfing.inthe.wavepark.shared.model.WeatherData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val response: WeatherResponseBody
)

@Serializable
data class WeatherResponseBody(
    val header: WeatherHeader,
    val body: WeatherBody
)

@Serializable
data class WeatherHeader(
    val resultCode: String,
    val resultMsg: String
)

@Serializable
data class WeatherBody(
    val items: WeatherItems
)

@Serializable
data class WeatherItems(
    val item: List<WeatherItem>
)

@Serializable
data class WeatherItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int
)

class WeatherApiService(private val httpClient: HttpClient) {
    
    suspend fun getUltraSrtFcst(
        serviceKey: String,
        baseDate: String,
        baseTime: String,
        nx: Int = 56,
        ny: Int = 121,
        dataType: String = "JSON",
        numOfRows: Int = 100
    ): WeatherResponse {
        return httpClient.get("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst") {
            parameter("serviceKey", serviceKey)
            parameter("base_date", baseDate)
            parameter("base_time", baseTime)
            parameter("nx", nx)
            parameter("ny", ny)
            parameter("dataType", dataType)
            parameter("numOfRows", numOfRows)
        }.body()
    }
} 