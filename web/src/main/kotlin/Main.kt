import androidx.compose.web.renderComposable
import com.surfing.inthe.wavepark.shared.ui.WeatherCard
import com.surfing.inthe.wavepark.shared.model.WeatherData
import kotlinx.datetime.Instant

fun main() {
    renderComposable(rootElementId = "root") {
        val sampleWeatherData = WeatherData(
            weatherStatus = "맑음",
            temper = "25",
            baseDate = "20241201",
            baseTime = "0600",
            fcstTime = "0700"
        )
        
        WeatherCard(weatherData = sampleWeatherData)
    }
} 