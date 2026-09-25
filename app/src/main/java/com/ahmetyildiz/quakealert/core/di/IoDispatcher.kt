package com.ahmetyildiz.quakealert.core.di

import javax.inject.Qualifier

/** Marks the [kotlinx.coroutines.CoroutineDispatcher] for blocking I/O (files, blocking platform APIs). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
