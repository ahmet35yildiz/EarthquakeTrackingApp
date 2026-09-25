package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.LocalAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {

    @Binds
    @Singleton
    fun bindAnalyticsTracker(tracker: LocalAnalyticsTracker): AnalyticsTracker
}
