package com.ahmetyildiz.quakealert.core.notification

enum class NotificationAccess {
    ALLOWED,
    APP_BLOCKED,
    ALERT_CHANNEL_BLOCKED,
    ;

    val isAllowed: Boolean
        get() = this == ALLOWED
}
