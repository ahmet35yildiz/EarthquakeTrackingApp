package com.ahmetyildiz.quakealert.navigation

import android.content.Intent

fun Intent.withDeepLinkHandledInPlace(): Intent {
    if (data?.scheme == NavigationConfig.DEEP_LINK_SCHEME) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    return this
}
