package com.ahmetyildiz.quakealert.core.ui.format

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.CountryNames
import com.ahmetyildiz.quakealert.core.model.CompassDirection
import com.ahmetyildiz.quakealert.core.model.PlaceDescription
import com.ahmetyildiz.quakealert.core.model.RegionPhrase
import com.ahmetyildiz.quakealert.core.model.RegionSide
import java.text.NumberFormat
import java.util.Locale

private const val MAX_DISTANCE_FRACTION_DIGITS: Int = 1
private const val NAME_SEPARATOR: String = ", "

fun formatPlace(place: String, resources: Resources): String {
    val locale: Locale = resources.configuration.locales[0]
    if (locale.language == Locale.ENGLISH.language) return place
    return when (val description: PlaceDescription = PlaceDescription.parse(place)) {
        is PlaceDescription.NearPlace -> resources.getString(
            R.string.place_near_format,
            formatDistance(description.distanceKm, locale),
            resources.getString(description.direction.labelRes),
            resources.translatePlaceName(description.placeName, locale),
        )
        is PlaceDescription.Named -> resources.translatePlaceName(description.name, locale)
    }
}

@Composable
fun localizedPlace(place: String?): String {
    if (place == null) return stringResource(R.string.earthquake_unknown_place)
    val resources: Resources = LocalResources.current
    val locale: Locale = currentLocale()
    return remember(place, locale, resources) { formatPlace(place, resources) }
}

private fun formatDistance(distanceKm: Double, locale: Locale): String =
    NumberFormat.getNumberInstance(locale)
        .apply { maximumFractionDigits = MAX_DISTANCE_FRACTION_DIGITS }
        .format(distanceKm)

private fun Resources.translatePlaceName(name: String, locale: Locale): String =
    name.split(NAME_SEPARATOR).joinToString(NAME_SEPARATOR) { translateName(it, locale) }

private fun Resources.translateName(name: String, locale: Locale): String {
    CountryNames.translate(name, locale)?.let { return it }
    PlaceNameDictionary.find(name)?.let { return getString(it) }
    return when (val phrase: RegionPhrase = RegionPhrase.parse(name)) {
        is RegionPhrase.SideOf ->
            getString(R.string.place_side_format, translateName(phrase.name, locale), getString(phrase.side.labelRes))
        is RegionPhrase.Region -> getString(R.string.place_region_format, translateName(phrase.name, locale))
        is RegionPhrase.OffCoast -> translateOffCoast(phrase, locale)
        is RegionPhrase.Islands -> getString(R.string.place_islands_format, phrase.name)
        is RegionPhrase.Island -> getString(R.string.place_island_format, phrase.name)
        is RegionPhrase.Plain -> name
    }
}

private fun Resources.translateOffCoast(phrase: RegionPhrase.OffCoast, locale: Locale): String {
    val name: String = translateName(phrase.name, locale)
    val coast: CompassDirection = phrase.coast ?: return getString(R.string.place_off_coast_format, name)
    return getString(R.string.place_off_side_coast_format, name, getString(coast.labelRes))
}

@get:StringRes
private val CompassDirection.labelRes: Int
    get() = when (this) {
        CompassDirection.NORTH -> R.string.direction_north
        CompassDirection.NORTH_NORTHEAST -> R.string.direction_north_northeast
        CompassDirection.NORTHEAST -> R.string.direction_northeast
        CompassDirection.EAST_NORTHEAST -> R.string.direction_east_northeast
        CompassDirection.EAST -> R.string.direction_east
        CompassDirection.EAST_SOUTHEAST -> R.string.direction_east_southeast
        CompassDirection.SOUTHEAST -> R.string.direction_southeast
        CompassDirection.SOUTH_SOUTHEAST -> R.string.direction_south_southeast
        CompassDirection.SOUTH -> R.string.direction_south
        CompassDirection.SOUTH_SOUTHWEST -> R.string.direction_south_southwest
        CompassDirection.SOUTHWEST -> R.string.direction_southwest
        CompassDirection.WEST_SOUTHWEST -> R.string.direction_west_southwest
        CompassDirection.WEST -> R.string.direction_west
        CompassDirection.WEST_NORTHWEST -> R.string.direction_west_northwest
        CompassDirection.NORTHWEST -> R.string.direction_northwest
        CompassDirection.NORTH_NORTHWEST -> R.string.direction_north_northwest
    }

@get:StringRes
private val RegionSide.labelRes: Int
    get() = when (this) {
        RegionSide.NORTH -> R.string.place_side_north
        RegionSide.SOUTH -> R.string.place_side_south
        RegionSide.EAST -> R.string.place_side_east
        RegionSide.WEST -> R.string.place_side_west
        RegionSide.NORTHEAST -> R.string.place_side_northeast
        RegionSide.NORTHWEST -> R.string.place_side_northwest
        RegionSide.SOUTHEAST -> R.string.place_side_southeast
        RegionSide.SOUTHWEST -> R.string.place_side_southwest
        RegionSide.CENTRAL -> R.string.place_side_central
    }
