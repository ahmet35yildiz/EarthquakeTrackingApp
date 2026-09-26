package com.ahmetyildiz.quakealert.core.navigation

import android.net.Uri
import androidx.core.net.toUri

object DeepLinkConfig {
    const val SCHEME: String = "quakealert"
    const val EARTHQUAKE_BASE: String = "$SCHEME://earthquake"
    private const val IS_FROM_NOTIFICATION_PARAMETER: String = "isFromNotification"

    fun createEarthquakeDetailUri(earthquakeId: String, isFromNotification: Boolean): Uri =
        "$EARTHQUAKE_BASE/${Uri.encode(earthquakeId)}?$IS_FROM_NOTIFICATION_PARAMETER=$isFromNotification".toUri()
}
