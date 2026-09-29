package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R

@Composable
internal fun OnboardingHeroIcon(icon: Painter) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
internal fun OnboardingAppIcon() {
    Box(
        modifier = Modifier
            .size(APP_ICON_SIZE)
            .shadow(elevation = APP_ICON_ELEVATION, shape = CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.requiredSize(APP_ICON_SIZE * ADAPTIVE_ICON_FULL_TO_VISIBLE_RATIO),
        )
    }
}

private val APP_ICON_SIZE: Dp = 144.dp
private val APP_ICON_ELEVATION: Dp = 6.dp
private const val ADAPTIVE_ICON_FULL_TO_VISIBLE_RATIO: Float = 108f / 72f
