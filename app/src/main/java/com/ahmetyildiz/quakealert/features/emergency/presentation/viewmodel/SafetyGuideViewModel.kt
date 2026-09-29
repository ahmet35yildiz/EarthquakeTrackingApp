package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SafetyGuideSectionValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SafetyGuideViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val isFirstOpen: Boolean = !savedStateHandle.contains(KEY_SELECTED_SECTION)
    val selectedSection: StateFlow<SafetyGuideSection> =
        savedStateHandle.getStateFlow(KEY_SELECTED_SECTION, SafetyGuideSection.BEFORE)

    init {
        if (isFirstOpen) trackViewed(selectedSection.value)
    }

    fun onSectionSelected(section: SafetyGuideSection) {
        if (section == selectedSection.value) return
        savedStateHandle[KEY_SELECTED_SECTION] = section
        trackViewed(section)
    }

    private fun trackViewed(section: SafetyGuideSection) {
        analyticsTracker.track(AnalyticsEvent.SafetyGuideViewed(section.toParameterValue()))
    }

    private fun SafetyGuideSection.toParameterValue(): SafetyGuideSectionValue = when (this) {
        SafetyGuideSection.BEFORE -> SafetyGuideSectionValue.BEFORE
        SafetyGuideSection.DURING -> SafetyGuideSectionValue.DURING
        SafetyGuideSection.AFTER -> SafetyGuideSectionValue.AFTER
    }

    private companion object {
        const val KEY_SELECTED_SECTION: String = "safety_guide_selected_section"
    }
}
