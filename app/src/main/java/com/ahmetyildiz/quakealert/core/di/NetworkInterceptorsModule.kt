package com.ahmetyildiz.quakealert.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import okhttp3.Interceptor

/**
 * Declares the set of OkHttp interceptors so it may be empty (release builds). Build-type source sets contribute
 * to it with `@IntoSet`, e.g. `src/debug/.../DebugNetworkModule`.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NetworkInterceptorsModule {

    @Multibinds
    fun bindInterceptors(): Set<Interceptor>
}
