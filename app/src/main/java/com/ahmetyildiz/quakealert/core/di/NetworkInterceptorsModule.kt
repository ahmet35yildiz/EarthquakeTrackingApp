package com.ahmetyildiz.quakealert.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
interface NetworkInterceptorsModule {

    @Multibinds
    fun bindInterceptors(): Set<Interceptor>
}
