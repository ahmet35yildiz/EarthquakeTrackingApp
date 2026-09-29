package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaMode
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingPage
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val clicks: MutableList<String> = mutableListOf()
    private val actions = OnboardingActions(
        onNext = { clicks += "next" },
        onBack = { clicks += "back" },
        onThresholdChanged = {},
        onAreaSelectionChanged = {},
        onAllowNotifications = { clicks += "allow" },
        onOpenNotificationSettings = { clicks += "settings" },
        onFinish = { clicks += "finish" },
    )

    @Test
    fun welcomeShowsTheDisclaimerAndStartsTheSetup() {
        setContent(OnboardingUiState(page = OnboardingPage.WELCOME))
        composeRule.onNodeWithText(string(R.string.onboarding_disclaimer)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.onboarding_step_indicator, 1, 3)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_get_started)).performClick()
        assertEquals(listOf("next"), clicks)
    }

    @Test
    fun setupWithoutACityCannotContinue() {
        val pending = AreaSelection(AreaMode.NEAR_CITY, city = null, radiusKm = 250)
        setContent(OnboardingUiState(page = OnboardingPage.ALERT_SETUP, areaSelection = pending))
        composeRule.onNodeWithText(string(R.string.onboarding_setup_city_required)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_next)).assertIsNotEnabled()
    }

    @Test
    fun setupPageGoesBack() {
        setContent(OnboardingUiState(page = OnboardingPage.ALERT_SETUP))
        composeRule.onNodeWithText(string(R.string.action_next)).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.action_back)).performClick()
        assertEquals(listOf("next", "back"), clicks)
    }

    @Test
    fun notificationsCanBeRequestedOrSkipped() {
        setContent(OnboardingUiState(page = OnboardingPage.NOTIFICATIONS))
        composeRule.onNodeWithText(string(R.string.action_allow_notifications)).performClick()
        composeRule.onNodeWithText(string(R.string.action_not_now)).performClick()
        assertEquals(listOf("allow", "finish"), clicks)
    }

    @Test
    fun withoutARuntimePermissionTheStepOnlyConfirms() {
        setContent(
            OnboardingUiState(page = OnboardingPage.NOTIFICATIONS, notificationAccess = NotificationAccess.ALLOWED),
            isRequestSupported = false,
        )
        composeRule.onNodeWithText(string(R.string.notifications_allowed)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_allow_notifications)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.action_finish)).performClick()
        assertEquals(listOf("finish"), clicks)
    }

    @Test
    fun deniedPermissionOffersTheSettingsAndStillFinishes() {
        setContent(OnboardingUiState(page = OnboardingPage.NOTIFICATIONS, isPermissionDenied = true))
        composeRule.onNodeWithText(string(R.string.onboarding_notifications_denied)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performClick()
        composeRule.onNodeWithText(string(R.string.action_finish)).performClick()
        assertEquals(listOf("settings", "finish"), clicks)
    }

    @Test
    fun blockedAlertChannelOpensItsSettingsInsteadOfThePermissionDialog() {
        setContent(OnboardingUiState(page = OnboardingPage.NOTIFICATIONS, notificationAccess = NotificationAccess.ALERT_CHANNEL_BLOCKED))
        val channelName: String = string(R.string.notification_channel_alerts_name)
        composeRule.onNodeWithText(string(R.string.notifications_channel_blocked_message, channelName)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_allow_notifications)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performClick()
        composeRule.onNodeWithText(string(R.string.action_finish)).performClick()
        assertEquals(listOf("settings", "finish"), clicks)
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setContent(uiState: OnboardingUiState, isRequestSupported: Boolean = true) {
        composeRule.setContent {
            QuakeAlertTheme {
                OnboardingScreen(uiState = uiState, actions = actions, isPermissionRequestSupported = isRequestSupported) {
                    Text(text = "Area selector")
                }
            }
        }
    }
}
