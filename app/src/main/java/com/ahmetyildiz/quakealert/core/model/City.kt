package com.ahmetyildiz.quakealert.core.model

/** A named place the user picked as the center of their alert area. */
data class City(
    val name: String,
    /** Province or state, e.g. "İzmir" or "California"; null when the geocoder does not provide one. */
    val adminArea: String?,
    /** ISO 3166-1 alpha-2 code, e.g. "TR". */
    val countryCode: String,
    val location: GeoPoint,
)
