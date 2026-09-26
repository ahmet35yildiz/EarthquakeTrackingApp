package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertSettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AlertSettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val now: Instant = Instant.parse("2026-09-26T12:00:00Z")
    private val izmirArea = AlertArea.AroundCity(City("İzmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250)
    private val toggles: MutableList<Boolean> = mutableListOf()
    private var openSettingsCount: Int = 0
    private val actions = AlertSettingsActions(
        onAlertsToggled = { toggles += it },
        onThresholdChanged = {},
        onAreaSelectionChanged = {},
        onOpenNotificationSettings = { openSettingsCount++ },
    )

    @Test
    fun enabledAlertsSummarizeThresholdAndArea() {
        setContent(AlertSettingsUiState(isLoading = false, settings = AlertSettings.DEFAULT.copy(area = izmirArea)))
        val magnitude: String = string(R.string.magnitude_value, 4.5)
        composeRule.onNodeWithText(string(R.string.alerts_summary_city, magnitude, 250, "İzmir")).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alerts_switch_title)).assertIsOn()
    }

    @Test
    fun switchReportsTheNewState() {
        setContent(AlertSettingsUiState(isLoading = false))
        composeRule.onNodeWithText(string(R.string.alerts_switch_title)).performClick()
        assertEquals(listOf(false), toggles)
    }

    @Test
    fun disabledAlertsExplainThatTheListStillUsesTheChoice() {
        setContent(AlertSettingsUiState(isLoading = false, settings = AlertSettings.DEFAULT.copy(isEnabled = false)))
        composeRule.onNodeWithText(string(R.string.alerts_summary_off)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alerts_switch_title)).assertIsOff()
    }

    @Test
    fun blockedNotificationsOfferTheSystemSettings() {
        setContent(AlertSettingsUiState(isLoading = false, areNotificationsAllowed = false))
        composeRule.onNodeWithText(string(R.string.notifications_blocked)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performScrollTo().performClick()
        assertEquals(1, openSettingsCount)
    }

    @Test
    fun lastCheckIsShownAsRelativeTime() {
        setContent(AlertSettingsUiState(isLoading = false, lastCheckedAt = now - Duration.ofMinutes(6)))
        val relative: String = composeRule.activity.resources.getQuantityString(R.plurals.relative_time_minutes_ago, 6, 6)
        composeRule.onNodeWithText(string(R.string.last_checked, relative)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun missingCheckSaysSo() {
        setContent(AlertSettingsUiState(isLoading = false))
        composeRule.onNodeWithText(string(R.string.last_checked_never)).performScrollTo().assertIsDisplayed()
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setContent(uiState: AlertSettingsUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                AlertSettingsScreen(uiState = uiState, actions = actions, now = now) { Text(text = "Area selector") }
            }
        }
    }
}
