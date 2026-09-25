package com.ahmetyildiz.quakealert.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/** Debug builds only: logs request lines, status and timing to Logcat (bodies are too large to be useful). */
@Module
@InstallIn(SingletonComponent::class)
object DebugNetworkModule {

    @Provides
    @IntoSet
    fun provideLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
}
