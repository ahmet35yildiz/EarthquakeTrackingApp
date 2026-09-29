package com.ahmetyildiz.quakealert.navigation

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ahmetyildiz.quakealert.MainActivity
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainTabsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule: ComposeTestRule = createEmptyComposeRule()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
        val configuration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        runBlocking { preferencesRepository.setOnboardingCompleted(true) }
    }

    @Test
    fun statisticsTabOpensItsScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            clickTab(scenario.string(R.string.tab_statistics))
            awaitText(scenario.string(R.string.statistics_period_30_days))
        }
    }

    @Test
    fun emergencyTabOpensTheSafetyGuideAndBackReturnsToTheTab() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            clickTab(scenario.string(R.string.tab_emergency))
            awaitText(scenario.string(R.string.emergency_title))
            composeRule.onNodeWithText(scenario.string(R.string.safety_guide_title)).performClick()
            awaitText(scenario.firstBeforeItem())
            composeRule.onNodeWithText(scenario.string(R.string.tab_emergency)).assertDoesNotExist()
            composeRule.onNodeWithContentDescription(scenario.string(R.string.action_back)).performClick()
            awaitText(scenario.string(R.string.emergency_title))
            composeRule.onNodeWithText(scenario.string(R.string.tab_emergency)).assertIsDisplayed()
        }
    }

    private fun clickTab(label: String) {
        awaitText(label)
        composeRule.onNodeWithText(label).performClick()
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun ActivityScenario<MainActivity>.firstBeforeItem(): String {
        var text = ""
        onActivity { activity -> text = activity.resources.getStringArray(R.array.safety_guide_before_items).first() }
        return text
    }

    private fun ActivityScenario<MainActivity>.string(resId: Int): String {
        var text = ""
        onActivity { activity -> text = activity.getString(resId) }
        return text
    }

    private companion object {
        const val WAIT_TIMEOUT_MILLIS: Long = 10_000
    }
}
