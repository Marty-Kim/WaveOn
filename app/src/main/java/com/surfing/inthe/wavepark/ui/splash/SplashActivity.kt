package com.surfing.inthe.wavepark.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.LaunchedEffect
import com.surfing.inthe.wavepark.MainActivity
import kotlinx.coroutines.delay

/**
 * Compose 기반 SplashActivity
 * - 배경색(서핑/바다 테마)
 * - 중앙에 서핑 관련 이미지(surf.png)
 * - 2초 뒤 MainActivity로 이동
 */
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 2초 후 MainActivity로 이동
            LaunchedEffect(Unit) {
                delay(2000)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
            // Compose UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0099CC)), // 바다/서핑 테마 컬러
                contentAlignment = Alignment.Center
            ) {
                // 중앙 이미지 (임시: 시스템 내장 아이콘 사용)
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_compass),
                    contentDescription = "서핑 일러스트",
                    modifier = Modifier.size(180.dp)
                )
            }
        }
    }
} 