package com.surfing.inthe.wavepark.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.surfing.inthe.wavepark.shared.model.WeatherData

@Composable
fun WeatherCard(
    weatherData: WeatherData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "날씨 정보",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "날씨 상태",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = weatherData.weatherStatus,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column {
                    Text(
                        text = "기온",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${weatherData.temper}°C",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "기준시간: ${weatherData.baseDate} ${weatherData.baseTime}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 