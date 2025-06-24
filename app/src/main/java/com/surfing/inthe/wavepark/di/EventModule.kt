package com.surfing.inthe.wavepark.di

import com.surfing.inthe.wavepark.ui.home.EventRepository
import com.surfing.inthe.wavepark.ui.home.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Home 화면의 Repository 구현체를 DI로 주입하기 위한 Hilt 모듈.
 * @Binds를 사용해 인터페이스와 구현체를 연결.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EventModule {
    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository
} 