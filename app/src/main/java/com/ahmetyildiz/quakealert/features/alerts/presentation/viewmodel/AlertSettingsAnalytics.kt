package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate

fun AlertSettingsUpdate.toAnalyticsEvents(context: SetupContext): List<AnalyticsEvent> =
    listOfNotNull(
        AnalyticsEvent.AlertsToggled(isEnabled = updated.isEnabled)
            .takeIf { previous.isEnabled != updated.isEnabled },
        AnalyticsEvent.AlertThresholdChanged(from = previous.magnitudeThreshold, to = updated.magnitudeThreshold, context)
            .takeIf { previous.magnitudeThreshold != updated.magnitudeThreshold },
        toAreaEvent(context).takeIf { previous.area != updated.area },
    )

private fun AlertSettingsUpdate.toAreaEvent(context: SetupContext): AnalyticsEvent =
    when (val area: AlertArea = updated.area) {
        AlertArea.WholeWorld -> AnalyticsEvent.AlertAreaCleared(context = context)
        is AlertArea.AroundCity -> AnalyticsEvent.AlertAreaSet(area.city.countryCode, area.radiusKm, context)
    }
