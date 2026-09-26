package com.ahmetyildiz.quakealert

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ahmetyildiz.quakealert.core.di.ApplicationScope
import com.ahmetyildiz.quakealert.core.notification.NotificationChannels
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SyncAlertScheduleUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class QuakeAlertApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationChannels: NotificationChannels

    @Inject
    lateinit var syncAlertSchedule: SyncAlertScheduleUseCase

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        notificationChannels.register()
        applicationScope.launch { syncAlertSchedule() }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
