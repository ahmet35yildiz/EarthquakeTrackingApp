package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.notification.AndroidNotificationAccessChecker
import com.ahmetyildiz.quakealert.core.notification.NotificationAccessChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {

    @Binds
    fun bindNotificationAccessChecker(checker: AndroidNotificationAccessChecker): NotificationAccessChecker
}
