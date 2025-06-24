package com.surfing.inthe.wavepark.ui.splash

import javax.inject.Inject

/**
 * MVVM의 Repository 역할 (Splash 화면)
 * 데이터 소스(API, DB 등)와 ViewModel 사이의 추상화 계층.
 */
interface SplashRepository {
    fun isUserLoggedIn(): Boolean
}

/**
 * 실제 데이터 제공 구현체. (샘플)
 * @Inject 생성자: Hilt가 DI로 주입할 수 있게 함.
 */
class SplashRepositoryImpl @Inject constructor() : SplashRepository {
    override fun isUserLoggedIn(): Boolean {
        // 실제로는 SharedPreferences 등에서 로그인 상태 확인
        return false // 샘플: 항상 비로그인
    }
} 