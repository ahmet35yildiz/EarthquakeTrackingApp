package com.ahmetyildiz.quakealert.core.notification

fun interface NotificationPermissionChecker {

    fun areNotificationsAllowed(): Boolean
}
