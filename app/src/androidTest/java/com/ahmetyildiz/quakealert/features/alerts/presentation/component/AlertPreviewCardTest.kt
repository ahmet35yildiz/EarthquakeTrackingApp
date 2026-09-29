package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertPreviewUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertPreviewCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val days: Int = AlertConfig.PREVIEW_PERIOD.toDays().toInt()
    private val period: String by lazy {
        composeRule.activity.resources.getQuantityString(R.plurals.alert_preview_period, days, days)
    }

    @Test
    fun matchesAreCountedForThePreviewPeriod() {
        setContent(ready(matchCount = 5))
        val expected: String = composeRule.activity.resources.getQuantityString(R.plurals.alert_preview_matches, 5, 5, period)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun noMatchHasItsOwnSentence() {
        setContent(ready(matchCount = 0))
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.alert_preview_none, period)).assertIsDisplayed()
    }

    @Test
    fun unavailableDataIsExplained() {
        setContent(AlertPreviewUiState.Unavailable)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.alert_preview_unavailable)).assertIsDisplayed()
    }

    @Test
    fun hiddenShowsNothing() {
        setContent(AlertPreviewUiState.Hidden)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.alert_preview_loading)).assertDoesNotExist()
    }

    private fun ready(matchCount: Int): AlertPreviewUiState =
        AlertPreviewUiState.Ready(AlertPreview(matchCount = matchCount, period = AlertConfig.PREVIEW_PERIOD))

    private fun setContent(state: AlertPreviewUiState) {
        composeRule.setContent { QuakeAlertTheme { AlertPreviewCard(state = state) } }
    }
}
