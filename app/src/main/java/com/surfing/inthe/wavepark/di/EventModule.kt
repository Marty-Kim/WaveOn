package com.surfing.inthe.wavepark.di

import com.surfing.inthe.wavepark.data.repository.EventRepository
import com.surfing.inthe.wavepark.data.repository.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Event Repository를 DI로 주입하기 위한 Hilt 모듈.
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