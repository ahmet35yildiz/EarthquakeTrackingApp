package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.ahmetyildiz.quakealert.core.model.GeoPoint

fun mapsIntent(location: GeoPoint, label: String): Intent {
    val coordinates = "${location.latitude},${location.longitude}"
    val uri: Uri = "geo:$coordinates?q=$coordinates(${Uri.encode(label)})".toUri()
    return Intent(Intent.ACTION_VIEW, uri)
}

fun shareIntent(text: String, chooserTitle: String): Intent {
    val send: Intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, chooserTitle)
}
