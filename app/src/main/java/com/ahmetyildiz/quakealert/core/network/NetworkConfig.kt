package com.ahmetyildiz.quakealert.core.network

/** Tunable network settings. */
object NetworkConfig {
    /** USGS FDSN event service host; endpoint paths are declared on the Retrofit interfaces. */
    const val USGS_BASE_URL: String = "https://earthquake.usgs.gov/"
    const val CONNECT_TIMEOUT_SECONDS: Long = 15
    /** The 7-day list response is ~265 KB; generous enough for slow mobile networks. */
    const val READ_TIMEOUT_SECONDS: Long = 30
}
