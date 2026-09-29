package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SafetyGuideSectionValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SafetyGuideViewModelTest {

    private val savedStateHandle = SavedStateHandle()
    private val analyticsTracker = FakeAnalyticsTracker()

    @Test
    fun `guide opens on the before section and records it`() {
        val viewModel = SafetyGuideViewModel(savedStateHandle, analyticsTracker)
        assertEquals(SafetyGuideSection.BEFORE, viewModel.selectedSection.value)
        assertEquals(listOf(viewed(SafetyGuideSectionValue.BEFORE)), analyticsTracker.events)
    }

    @Test
    fun `every section change is recorded`() {
        val viewModel = SafetyGuideViewModel(savedStateHandle, analyticsTracker)
        viewModel.onSectionSelected(SafetyGuideSection.DURING)
        viewModel.onSectionSelected(SafetyGuideSection.AFTER)
        assertEquals(SafetyGuideSection.AFTER, viewModel.selectedSection.value)
        val expected: List<AnalyticsEvent> = listOf(
            viewed(SafetyGuideSectionValue.BEFORE),
            viewed(SafetyGuideSectionValue.DURING),
            viewed(SafetyGuideSectionValue.AFTER),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    @Test
    fun `selecting the shown section again records nothing`() {
        val viewModel = SafetyGuideViewModel(savedStateHandle, analyticsTracker)
        viewModel.onSectionSelected(SafetyGuideSection.BEFORE)
        assertEquals(1, analyticsTracker.events.size)
    }

    @Test
    fun `restored screen keeps its section without recording it again`() {
        SafetyGuideViewModel(savedStateHandle, analyticsTracker).onSectionSelected(SafetyGuideSection.AFTER)
        val restored = SafetyGuideViewModel(savedStateHandle, analyticsTracker)
        assertEquals(SafetyGuideSection.AFTER, restored.selectedSection.value)
        assertEquals(2, analyticsTracker.events.size)
    }

    private fun viewed(section: SafetyGuideSectionValue): AnalyticsEvent = AnalyticsEvent.SafetyGuideViewed(section)
}
