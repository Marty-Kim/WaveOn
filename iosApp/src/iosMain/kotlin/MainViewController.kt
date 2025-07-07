import androidx.compose.ui.window.ComposeUIViewController
import com.surfing.inthe.wavepark.shared.ui.WeatherCard
import com.surfing.inthe.wavepark.shared.model.WeatherData

fun MainViewController() = ComposeUIViewController {
    val sampleWeatherData = WeatherData(
        weatherStatus = "맑음",
        temper = "25",
        baseDate = "20241201",
        baseTime = "0600",
        fcstTime = "0700"
    )
    
    WeatherCard(weatherData = sampleWeatherData)
} 