package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.datastore.DataStoreUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    @Binds
    @Singleton
    fun bindUserPreferencesRepository(repository: DataStoreUserPreferencesRepository): UserPreferencesRepository
}
