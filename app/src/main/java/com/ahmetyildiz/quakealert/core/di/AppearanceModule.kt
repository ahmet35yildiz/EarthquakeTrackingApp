package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.appearance.AppCompatThemeModeManager
import com.ahmetyildiz.quakealert.core.appearance.ThemeModeManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppearanceModule {

    @Binds
    fun bindThemeModeManager(manager: AppCompatThemeModeManager): ThemeModeManager
}
