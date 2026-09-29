package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AlertFeedbackViewModelTest {

    private val savedStateHandle = SavedStateHandle()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val viewModel = AlertFeedbackViewModel(savedStateHandle, analyticsTracker)

    @Test
    fun `nothing is recorded until the user answers`() {
        assertFalse(viewModel.isAnswered.value)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `an answer is recorded with the earthquake id`() {
        viewModel.onAnswered(eventId = "us7000abcd", isUseful = false)
        assertTrue(viewModel.isAnswered.value)
        assertEquals(listOf(AnalyticsEvent.AlertFeedbackGiven("us7000abcd", isUseful = false)), analyticsTracker.events)
    }

    @Test
    fun `only the first answer is recorded`() {
        viewModel.onAnswered(eventId = "us7000abcd", isUseful = true)
        viewModel.onAnswered(eventId = "us7000abcd", isUseful = false)
        assertEquals(1, analyticsTracker.events.size)
    }

    @Test
    fun `an answer survives a restored screen`() {
        viewModel.onAnswered(eventId = "us7000abcd", isUseful = true)
        val restored = AlertFeedbackViewModel(savedStateHandle, analyticsTracker)
        restored.onAnswered(eventId = "us7000abcd", isUseful = false)
        assertTrue(restored.isAnswered.value)
        assertEquals(1, analyticsTracker.events.size)
    }
}
