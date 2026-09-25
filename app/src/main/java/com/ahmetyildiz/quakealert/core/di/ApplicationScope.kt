package com.ahmetyildiz.quakealert.core.di

import javax.inject.Qualifier

/**
 * Marks the [kotlinx.coroutines.CoroutineScope] that lives as long as the app process, for work that must finish
 * even when the screen that started it is gone (e.g. storing an analytics event, DataStore writes).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
