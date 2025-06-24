package com.surfing.inthe.wavepark.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Splash 화면의 ViewModel (MVVM)
 * Repository를 DI로 주입받아 데이터를 LiveData로 노출.
 * UI는 ViewModel만 관찰, 데이터 소스와 분리.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val splashRepository: SplashRepository
) : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun checkLoginStatus() {
        _isLoggedIn.value = splashRepository.isUserLoggedIn()
    }
} 