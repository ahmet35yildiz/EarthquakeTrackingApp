package com.ahmetyildiz.quakealert.navigation

import android.content.Intent
import com.ahmetyildiz.quakealert.core.navigation.DeepLinkConfig

fun Intent.isAppDeepLink(): Boolean = data?.scheme == DeepLinkConfig.SCHEME

fun Intent.withDeepLinkHandledInPlace(): Intent {
    if (isAppDeepLink()) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    return this
}
