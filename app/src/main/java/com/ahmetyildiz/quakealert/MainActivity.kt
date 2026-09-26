package com.ahmetyildiz.quakealert

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.core.analytics.AppOpenTracker
import com.ahmetyildiz.quakealert.core.notification.getAlertNotificationTapOrNull
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.navigation.QuakeAlertApp
import com.ahmetyildiz.quakealert.navigation.isAppDeepLink
import com.ahmetyildiz.quakealert.navigation.withDeepLinkHandledInPlace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appOpenTracker: AppOpenTracker

    private val pendingDeepLink = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent.withDeepLinkHandledInPlace()
        appOpenTracker.onActivityCreated(
            isRestored = savedInstanceState != null,
            notificationTap = intent.getAlertNotificationTapOrNull(),
        )
        setContent {
            val deepLink: Intent? by pendingDeepLink.collectAsStateWithLifecycle()
            QuakeAlertTheme {
                QuakeAlertApp(pendingDeepLink = deepLink, onDeepLinkHandled = { pendingDeepLink.value = null })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenTracker.onActivityResumed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent.withDeepLinkHandledInPlace())
        appOpenTracker.onNewIntent(intent.getAlertNotificationTapOrNull())
        if (intent.isAppDeepLink()) pendingDeepLink.value = intent
    }
}
