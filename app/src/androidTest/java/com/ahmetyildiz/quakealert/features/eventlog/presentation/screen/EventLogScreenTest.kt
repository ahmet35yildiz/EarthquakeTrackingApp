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
import com.ahmetyildiz.quakealert.features.eventlog.domain.AlertMetricsConfig
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.AlertMetrics
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.MetricRatio
import com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel.EventLogUiState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventLogScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val events: List<LoggedEvent> = listOf(
        LoggedEvent(2, "language_changed", mapOf("from" to "system", "to" to "tr"), Instant.parse("2026-09-26T18:12:01Z")),
        LoggedEvent(1, "app_opened", mapOf("source" to "launcher"), Instant.parse("2026-09-26T18:00:00Z")),
    )
    private val metrics = AlertMetrics(
        setupCompletion = MetricRatio(count = 1, total = 1),
        notificationOpens = MetricRatio(count = 4, total = 5),
        answeredOpens = MetricRatio(count = 3, total = 4),
        usefulAnswers = MetricRatio(count = 2, total = 3),
        notificationsFollowedByOptOut = MetricRatio(count = 1, total = 5),
        alertsTurnedOffAfterNotification = 1,
        thresholdsRaisedAfterNotification = 0,
        optOutWindow = AlertMetricsConfig.OPT_OUT_WINDOW,
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
    fun metricsAreShownAboveTheEventsWithCountsAndPercentages() {
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2, metrics = metrics))
        composeRule.onNodeWithText(string(R.string.metrics_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.metrics_ratio, 2, 3, 67)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.metrics_useful_answers_detail, 3, 4)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.metrics_opt_out_detail, 1, 0)).assertIsDisplayed()
    }

    @Test
    fun metricsWithoutDataShowNoPercentage() {
        val empty = metrics.copy(usefulAnswers = MetricRatio(count = 0, total = 0))
        setContent(EventLogUiState(isLoading = false, events = events, totalCount = 2, metrics = empty))
        composeRule.onNodeWithText(string(R.string.metrics_ratio_without_percent, 0, 0)).assertIsDisplayed()
    }

    @Test
    fun metricsAreHiddenWhileFiltering() {
        setContent(
            EventLogUiState(isLoading = false, query = "app", events = events.drop(1), totalCount = 2, metrics = metrics),
            query = "app",
        )
        composeRule.onNodeWithText(string(R.string.metrics_title)).assertDoesNotExist()
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
