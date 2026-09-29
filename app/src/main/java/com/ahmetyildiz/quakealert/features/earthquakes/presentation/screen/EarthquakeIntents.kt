package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.ahmetyildiz.quakealert.core.model.GeoPoint

private const val FELT_REPORT_PATH: String = "/tellus"

fun mapsIntent(location: GeoPoint, label: String): Intent {
    val coordinates = "${location.latitude},${location.longitude}"
    val uri: Uri = "geo:$coordinates?q=$coordinates(${Uri.encode(label)})".toUri()
    return Intent(Intent.ACTION_VIEW, uri)
}

fun feltReportUrl(detailUrl: String): String = detailUrl.trimEnd('/') + FELT_REPORT_PATH
