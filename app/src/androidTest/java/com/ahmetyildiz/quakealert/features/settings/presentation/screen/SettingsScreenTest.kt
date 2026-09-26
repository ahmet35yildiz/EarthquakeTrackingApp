package com.ahmetyildiz.quakealert.features.settings.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel.AppVersion
import com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val turkish = AppLanguage("tr")
    private val defaultState = SettingsUiState(
        languages = listOf(AppLanguage("en"), turkish),
        appVersion = AppVersion(name = "1.0", code = 1),
    )
    private val selectedLanguages: MutableList<AppLanguage?> = mutableListOf()
    private var openNotificationSettingsCount: Int = 0
    private var openUsgsCount: Int = 0
    private val actions = SettingsActions(
        onLanguageSelected = { selectedLanguages += it },
        onOpenNotificationSettings = { openNotificationSettingsCount++ },
        onOpenUsgsWebsite = { openUsgsCount++ },
    )

    @Test
    fun systemDefaultIsShownWhenNoLanguageIsPicked() {
        setContent(defaultState)
        composeRule.onNodeWithText(string(R.string.settings_language_system_default)).assertIsDisplayed()
    }

    @Test
    fun pickerListsLanguagesInTheirOwnLanguageAndReportsTheChoice() {
        setContent(defaultState)
        composeRule.onNodeWithText(string(R.string.settings_language_system_default)).performClick()
        composeRule.onNodeWithText(string(R.string.settings_language_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
        composeRule.onNodeWithText("Türkçe").performClick()
        assertEquals(listOf(turkish), selectedLanguages)
        composeRule.onNodeWithText(string(R.string.settings_language_dialog_title)).assertDoesNotExist()
    }

    @Test
    fun pickerMarksTheSelectedLanguage() {
        setContent(defaultState.copy(selectedLanguage = turkish))
        composeRule.onNodeWithText("Türkçe").performClick()
        composeRule.onNode(hasText("Türkçe") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText(string(R.string.settings_language_system_default)) and isSelectable())
            .assertIsNotSelected()
    }

    @Test
    fun blockedNotificationsOfferTheSystemSettings() {
        setContent(defaultState.copy(areNotificationsAllowed = false))
        composeRule.onNodeWithText(string(R.string.notifications_blocked)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performClick()
        assertEquals(1, openNotificationSettingsCount)
    }

    @Test
    fun aboutShowsDisclaimerVersionAndOpensUsgs() {
        setContent(defaultState)
        composeRule.onNodeWithText(string(R.string.settings_about_disclaimer_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.settings_about_version, "1.0", 1)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.settings_about_open_usgs)).performScrollTo().performClick()
        assertEquals(1, openUsgsCount)
    }

    @Test
    fun developerToolsEntryOpensTheDeveloperScreen() {
        var openDeveloperToolsCount = 0
        setContent(defaultState, onOpenDeveloperTools = { openDeveloperToolsCount++ })
        composeRule.onNodeWithText(string(R.string.developer_tools_title)).performScrollTo().performClick()
        assertEquals(1, openDeveloperToolsCount)
    }

    @Test
    fun developerToolsEntryIsHiddenWithoutADestination() {
        setContent(defaultState)
        composeRule.onNodeWithText(string(R.string.developer_tools_title)).assertDoesNotExist()
    }

    private fun setContent(uiState: SettingsUiState, onOpenDeveloperTools: (() -> Unit)? = null) {
        composeRule.setContent {
            QuakeAlertTheme {
                SettingsScreen(uiState = uiState, actions = actions, onOpenDeveloperTools = onOpenDeveloperTools)
            }
        }
    }

    private fun string(resId: Int, vararg args: Any): String = composeRule.activity.getString(resId, *args)
}
