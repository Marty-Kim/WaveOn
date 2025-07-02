package com.surfing.inthe.wavepark.data.api


import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    
    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtFcst")
    suspend fun getUltraSrtFcst(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int = 56,
        @Query("ny") ny: Int = 121,
        @Query("dataType") dataType: String = "JSON",
        @Query("numOfRows") numOfRows: Int = 100
    ): WeatherResponse
}

data class WeatherResponse(
    val response: WeatherResponseBody
)

data class WeatherResponseBody(
    val header: WeatherHeader,
    val body: WeatherBody
)

data class WeatherHeader(
    val resultCode: String,
    val resultMsg: String
)

data class WeatherBody(
    val items: WeatherItems
)

data class WeatherItems(
    val item: List<WeatherItem>
)

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