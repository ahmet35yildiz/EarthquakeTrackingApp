package com.ahmetyildiz.quakealert

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.navigation.QuakeAlertApp
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity (not ComponentActivity) so the per-app language API also works below Android 13.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuakeAlertTheme {
                QuakeAlertApp()
            }
        }
    }
}
