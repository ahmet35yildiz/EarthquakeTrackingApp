package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

enum class AreaMode { WHOLE_WORLD, NEAR_CITY }

data class AreaSelection(
    val mode: AreaMode,
    val city: City?,
    val radiusKm: Int,
) {

    fun toAlertAreaOrNull(): AlertArea? =
        when (mode) {
            AreaMode.WHOLE_WORLD -> AlertArea.WholeWorld
            AreaMode.NEAR_CITY -> city?.let { AlertArea.AroundCity(city = it, radiusKm = radiusKm) }
        }

    companion object {
        fun from(area: AlertArea): AreaSelection =
            when (area) {
                AlertArea.WholeWorld -> AreaSelection(
                    mode = AreaMode.WHOLE_WORLD,
                    city = null,
                    radiusKm = AlertConfig.DEFAULT_RADIUS_KM,
                )
                is AlertArea.AroundCity -> AreaSelection(
                    mode = AreaMode.NEAR_CITY,
                    city = area.city,
                    radiusKm = area.radiusKm,
                )
            }
    }
}
