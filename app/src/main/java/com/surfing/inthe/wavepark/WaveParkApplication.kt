package com.surfing.inthe.wavepark

import android.app.Application
import com.bumptech.glide.annotation.GlideModule
import com.surfing.inthe.wavepark.util.CrashReporter
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt DI를 사용하기 위한 Application 클래스.
 * @HiltAndroidApp 어노테이션을 붙이면 Hilt가 DI 그래프를 자동 생성.
 */
@HiltAndroidApp
class WaveOnApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // CrashReporter 초기화
        CrashReporter.getInstance(this)
    }
}