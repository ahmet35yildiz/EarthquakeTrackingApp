package com.ahmetyildiz.quakealert.features.emergency.presentation.component

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOnEffect(isEnabled: Boolean) {
    val view: View = LocalView.current
    DisposableEffect(view, isEnabled) {
        view.keepScreenOn = isEnabled
        onDispose { view.keepScreenOn = false }
    }
}
