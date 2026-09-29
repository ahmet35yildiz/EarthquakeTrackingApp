package com.ahmetyildiz.quakealert.core.notification

fun interface NotificationAccessChecker {

    fun getAlertNotificationAccess(): NotificationAccess
}
