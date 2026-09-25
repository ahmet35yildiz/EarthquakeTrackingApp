package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.locale.AppCompatLanguageManager
import com.ahmetyildiz.quakealert.core.locale.AppLanguageManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface LocaleModule {

    @Binds
    fun bindAppLanguageManager(manager: AppCompatLanguageManager): AppLanguageManager
}
