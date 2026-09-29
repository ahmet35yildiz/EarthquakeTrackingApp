package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AlertFeedbackViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    val isAnswered: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_IS_ANSWERED, false)

    fun onAnswered(eventId: String, isUseful: Boolean) {
        if (isAnswered.value) return
        savedStateHandle[KEY_IS_ANSWERED] = true
        analyticsTracker.track(AnalyticsEvent.AlertFeedbackGiven(eventId = eventId, isUseful = isUseful))
    }

    private companion object {
        const val KEY_IS_ANSWERED: String = "alert_feedback_answered"
    }
}
