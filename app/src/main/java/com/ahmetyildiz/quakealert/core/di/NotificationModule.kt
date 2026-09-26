package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.notification.AndroidNotificationPermissionChecker
import com.ahmetyildiz.quakealert.core.notification.NotificationPermissionChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {

    @Binds
    fun bindNotificationPermissionChecker(checker: AndroidNotificationPermissionChecker): NotificationPermissionChecker
}
