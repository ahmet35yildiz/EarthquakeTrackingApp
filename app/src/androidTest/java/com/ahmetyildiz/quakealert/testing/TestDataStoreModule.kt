package com.ahmetyildiz.quakealert.testing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.ahmetyildiz.quakealert.core.di.ApplicationScope
import com.ahmetyildiz.quakealert.core.di.DataStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.UUID
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataStoreModule::class])
object TestDataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope applicationScope: CoroutineScope,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = applicationScope,
            produceFile = { File(context.cacheDir, "test_${UUID.randomUUID()}.preferences_pb") },
        )
}
