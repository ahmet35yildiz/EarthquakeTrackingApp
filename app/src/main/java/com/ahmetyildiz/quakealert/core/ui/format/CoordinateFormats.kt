package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import java.text.NumberFormat
import kotlin.math.abs

private const val COORDINATE_FRACTION_DIGITS: Int = 2

@Composable
fun formatCoordinates(point: GeoPoint): String {
    val latitudeHemisphere: Int = if (point.latitude >= 0) R.string.coordinate_north else R.string.coordinate_south
    val longitudeHemisphere: Int = if (point.longitude >= 0) R.string.coordinate_east else R.string.coordinate_west
    val latitude: String = formatCoordinate(point.latitude, latitudeHemisphere)
    val longitude: String = formatCoordinate(point.longitude, longitudeHemisphere)
    return stringResource(R.string.coordinate_pair, latitude, longitude)
}

@Composable
private fun formatCoordinate(value: Double, hemisphereRes: Int): String {
    val number: String = NumberFormat.getNumberInstance(currentLocale()).apply {
        minimumFractionDigits = COORDINATE_FRACTION_DIGITS
        maximumFractionDigits = COORDINATE_FRACTION_DIGITS
    }.format(abs(value))
    return stringResource(R.string.coordinate_value, number, stringResource(hemisphereRes))
}
