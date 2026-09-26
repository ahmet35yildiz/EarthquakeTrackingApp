package com.ahmetyildiz.quakealert.features.eventlog.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel.EventLogUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class EventLogScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val events: List<LoggedEvent> = listOf(
        LoggedEvent(2, "language_changed", mapOf("from" to "system", "to" to "tr"), Instant.parse("2026-09-26T18:12:01Z")),
        LoggedEvent(1, "app_opened", mapOf("source" to "launcher"), Instant.parse("2026-09-26T18:00:00Z")),
    )
    private val queries: MutableList<String> = mutableListOf()
    private var shareCount: Int = 0
    private var clearCount: Int = 0
    private val actions = EventLogActions(
        onBack = {},
        onQueryChange = { queries += it },
        onShare = { shareCount++ },
        onClearConfirmed = { clearCount++ },
    )

    @Test
    fun eventsShowNameParamsAndCount() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2))
        composeRule.onNodeWithText("language_changed").assertIsDisplayed()
        composeRule.onNodeWithText("to=tr").assertIsDisplayed()
        composeRule.onNodeWithText(quantityString(R.plurals.event_log_count, 2, 2)).assertIsDisplayed()
    }

    @Test
    fun typingReportsTheQuery() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2))
        composeRule.onNodeWithText(string(R.string.event_log_filter_label)).performTextInput("app")
        assertEquals(listOf("app"), queries)
    }

    @Test
    fun filteredListShowsVisibleOfTotal() {
        setContent(EventLogUiState(isLoading = false, query = "app", events = events.drop(1), totalCount = 2), query = "app")
        composeRule.onNodeWithText(quantityString(R.plurals.event_log_filtered_count, 2, 1, 2)).assertIsDisplayed()
    }

    @Test
    fun filterWithoutMatchExplainsIt() {
        setContent(EventLogUiState(isLoading = false, query = "detail", totalCount = 2), query = "detail")
        composeRule.onNodeWithText(string(R.string.event_log_no_match_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.event_log_no_match_message, "detail")).assertIsDisplayed()
    }

    @Test
    fun emptyLogShowsEmptyStateAndDisablesActions() {
        setContent(EventLogUiState(isLoading = false))
        composeRule.onNodeWithText(string(R.string.event_log_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.action_share)).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(string(R.string.action_clear_event_log)).assertIsNotEnabled()
    }

    @Test
    fun shareReportsTheAction() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2))
        composeRule.onNodeWithContentDescription(string(R.string.action_share)).performClick()
        assertEquals(1, shareCount)
    }

    @Test
    fun clearAsksForConfirmationFirst() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2))
        composeRule.onNodeWithContentDescription(string(R.string.action_clear_event_log)).performClick()
        composeRule.onNodeWithText(quantityString(R.plurals.event_log_clear_dialog_message, 2, 2)).assertIsDisplayed()
        assertEquals(0, clearCount)
        composeRule.onNodeWithText(string(R.string.action_clear)).performClick()
        assertEquals(1, clearCount)
        composeRule.onNodeWithText(string(R.string.event_log_clear_dialog_title)).assertDoesNotExist()
    }

    @Test
    fun cancellingTheDialogKeepsTheLog() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2))
        composeRule.onNodeWithContentDescription(string(R.string.action_clear_event_log)).performClick()
        composeRule.onNodeWithText(string(R.string.action_cancel)).performClick()
        assertEquals(0, clearCount)
    }

    private fun setContent(uiState: EventLogUiState, query: String = "") {
        composeRule.setContent {
            QuakeAlertTheme { EventLogScreen(uiState = uiState, query = query, actions = actions) }
        }
    }

    private fun string(resId: Int, vararg args: Any): String = composeRule.activity.getString(resId, *args)

    private fun quantityString(resId: Int, quantity: Int, vararg args: Any): String =
        composeRule.activity.resources.getQuantityString(resId, quantity, *args)
}
