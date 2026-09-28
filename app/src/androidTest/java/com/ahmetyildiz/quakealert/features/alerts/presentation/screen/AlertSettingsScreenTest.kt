package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

@RunWith(AndroidJUnit4::class)
class AlertSettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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
    fun blockedNotificationsReplaceTheSummaryAndOfferTheSystemSettings() {
        setContent(AlertSettingsUiState(isLoading = false, areNotificationsAllowed = false))
        composeRule.onNodeWithText(string(R.string.alerts_blocked_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alerts_summary_world, string(R.string.magnitude_value, 4.5)))
            .assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.notifications_blocked)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.action_allow_notifications)).performClick()
        assertEquals(1, openSettingsCount)
    }

    @Test
    fun blockedNotificationsAreNotFlaggedWhileAlertsAreOff() {
        setContent(
            AlertSettingsUiState(
                isLoading = false,
                settings = AlertSettings.DEFAULT.copy(isEnabled = false),
                areNotificationsAllowed = false,
            ),
        )
        composeRule.onNodeWithText(string(R.string.alerts_summary_off)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alerts_blocked_title)).assertDoesNotExist()
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setContent(uiState: AlertSettingsUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                AlertSettingsScreen(uiState = uiState, actions = actions) { Text(text = "Area selector") }
            }
        }
    }
}
