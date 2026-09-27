package com.ahmetyildiz.quakealert.core.ui.format

import androidx.annotation.StringRes
import com.ahmetyildiz.quakealert.R
import java.util.Locale

internal object PlaceNameDictionary {

    private val TRANSLATED_NAMES: Map<String, Int> = mapOf(
        "south sandwich islands" to R.string.place_name_south_sandwich_islands,
        "aleutian islands" to R.string.place_name_aleutian_islands,
        "south shetland islands" to R.string.place_name_south_shetland_islands,
        "loyalty islands" to R.string.place_name_loyalty_islands,
        "azores islands" to R.string.place_name_azores_islands,
        "philippine islands" to R.string.place_name_philippine_islands,
        "easter island" to R.string.place_name_easter_island,
        "south georgia island" to R.string.place_name_south_georgia_island,
        "north island of new zealand" to R.string.place_name_north_island_of_new_zealand,
        "island of hawaii" to R.string.place_name_island_of_hawaii,
        "mid-atlantic ridge" to R.string.place_name_mid_atlantic_ridge,
        "mid-indian ridge" to R.string.place_name_mid_indian_ridge,
        "east pacific rise" to R.string.place_name_east_pacific_rise,
        "pacific-antarctic ridge" to R.string.place_name_pacific_antarctic_ridge,
        "indian-antarctic ridge" to R.string.place_name_indian_antarctic_ridge,
        "southwest indian ridge" to R.string.place_name_southwest_indian_ridge,
        "southeast indian ridge" to R.string.place_name_southeast_indian_ridge,
        "reykjanes ridge" to R.string.place_name_reykjanes_ridge,
        "carlsberg ridge" to R.string.place_name_carlsberg_ridge,
        "west chile rise" to R.string.place_name_west_chile_rise,
        "owen fracture zone" to R.string.place_name_owen_fracture_zone,
        "galapagos triple junction" to R.string.place_name_galapagos_triple_junction,
        "indian ocean triple junction" to R.string.place_name_indian_ocean_triple_junction,
        "chagos archipelago" to R.string.place_name_chagos_archipelago,
        "drake passage" to R.string.place_name_drake_passage,
        "davis strait" to R.string.place_name_davis_strait,
        "mozambique channel" to R.string.place_name_mozambique_channel,
        "gulf of alaska" to R.string.place_name_gulf_of_alaska,
        "banda sea" to R.string.place_name_banda_sea,
        "scotia sea" to R.string.place_name_scotia_sea,
        "greenland sea" to R.string.place_name_greenland_sea,
        "norwegian sea" to R.string.place_name_norwegian_sea,
        "beaufort sea" to R.string.place_name_beaufort_sea,
        "arafura sea" to R.string.place_name_arafura_sea,
        "sea of okhotsk" to R.string.place_name_sea_of_okhotsk,
        "central america" to R.string.place_name_central_america,
        "africa" to R.string.place_name_africa,
        "north atlantic ocean" to R.string.place_name_north_atlantic_ocean,
        "south atlantic ocean" to R.string.place_name_south_atlantic_ocean,
        "north pacific ocean" to R.string.place_name_north_pacific_ocean,
        "south pacific ocean" to R.string.place_name_south_pacific_ocean,
        "north indian ocean" to R.string.place_name_north_indian_ocean,
        "south indian ocean" to R.string.place_name_south_indian_ocean,
        "kamchatka peninsula" to R.string.place_name_kamchatka_peninsula,
    )

    @StringRes
    fun find(englishName: String): Int? = TRANSLATED_NAMES[englishName.lowercase(Locale.ROOT)]
}
