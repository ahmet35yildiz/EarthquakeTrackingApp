package com.ahmetyildiz.quakealert.navigation

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ahmetyildiz.quakealert.MainActivity
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.di.NotificationModule
import com.ahmetyildiz.quakealert.core.notification.NotificationPermissionChecker
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.alerts.worker.AlertWorkScheduler
import com.ahmetyildiz.quakealert.testing.FakeUsgsApi
import com.ahmetyildiz.quakealert.testing.UsgsTestData
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NotificationModule::class)
@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule: ComposeTestRule = createEmptyComposeRule()

    @BindValue
    @JvmField
    val permissionChecker: NotificationPermissionChecker = NotificationPermissionChecker { true }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var usgsApi: FakeUsgsApi

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
        usgsApi.features = listOf(UsgsTestData.earthquake(EVENT_ID, magnitude = 4.6, place = PLACE, time = Instant.now()))
    }

    @Test
    fun firstLaunchWalksThroughOnboardingIntoTheEarthquakeList() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val string: (Int) -> String = { resId -> scenario.string(resId) }
            awaitText(string(R.string.onboarding_welcome_title))
            composeRule.onNodeWithText(string(R.string.action_get_started)).performClick()
            awaitText(string(R.string.onboarding_setup_title))
            composeRule.onNodeWithText(string(R.string.action_next)).performClick()
            awaitText(string(R.string.notifications_allowed))
            composeRule.onNodeWithText(string(R.string.action_finish)).performClick()
            awaitText(PLACE)
            composeRule.onNode(hasText(string(R.string.tab_earthquakes)) and isHeading()).assertIsDisplayed()
        }
        val preferences: UserPreferences = runBlocking { preferencesRepository.userPreferences.first() }
        assertTrue(preferences.isOnboardingCompleted)
        assertNotNull(preferences.alertBaselineAt)
        assertEquals(WorkInfo.State.ENQUEUED, periodicCheck().single().state)
    }

    @Test
    fun completedOnboardingOpensStraightOnTheEarthquakeList() {
        runBlocking { preferencesRepository.setOnboardingCompleted(true) }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitText(PLACE)
            composeRule.onNodeWithText(scenario.string(R.string.onboarding_welcome_title)).assertDoesNotExist()
        }
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun periodicCheck(): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(AlertWorkScheduler.PERIODIC_CHECK_WORK_NAME).get()

    private fun ActivityScenario<MainActivity>.string(resId: Int): String {
        var text = ""
        onActivity { activity -> text = activity.getString(resId) }
        return text
    }

    private companion object {
        const val EVENT_ID: String = "test0002"
        const val PLACE: String = "12 km SW of Seferihisar, Turkey"
        const val WAIT_TIMEOUT_MILLIS: Long = 10_000
    }
}
