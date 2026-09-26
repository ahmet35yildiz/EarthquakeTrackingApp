package com.ahmetyildiz.quakealert.features.eventlog.di

import com.ahmetyildiz.quakealert.features.eventlog.data.repository.EventLogRepositoryImpl
import com.ahmetyildiz.quakealert.features.eventlog.domain.repository.EventLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface EventLogModule {

    @Binds
    fun bindEventLogRepository(repository: EventLogRepositoryImpl): EventLogRepository
}
