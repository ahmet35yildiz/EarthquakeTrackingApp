package com.ahmetyildiz.quakealert

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.AppOpenSource
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.navigation.QuakeAlertApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// AppCompatActivity (not ComponentActivity) so the per-app language API also works below Android 13.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // A recreation (rotation, language switch) restores state and is not a new open.
        if (savedInstanceState == null) {
            analyticsTracker.track(AnalyticsEvent.AppOpened(source = AppOpenSource.LAUNCHER))
        }
        setContent {
            QuakeAlertTheme {
                QuakeAlertApp()
            }
        }
    }
}
