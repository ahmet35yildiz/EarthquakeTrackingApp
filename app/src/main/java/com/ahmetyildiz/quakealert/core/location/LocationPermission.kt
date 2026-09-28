package com.ahmetyildiz.quakealert.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

const val LOCATION_PERMISSION: String = Manifest.permission.ACCESS_COARSE_LOCATION

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED
