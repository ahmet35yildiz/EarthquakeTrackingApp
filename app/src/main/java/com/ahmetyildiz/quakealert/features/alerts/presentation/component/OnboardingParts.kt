package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import kotlin.math.cos
import kotlin.math.sin

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
internal fun OnboardingHeroIllustration(icon: Painter) {
    val ringColor: Color = MaterialTheme.colorScheme.primary
    val dashedRingColor: Color = MaterialTheme.colorScheme.outlineVariant
    val accentColor: Color = QuakeAlertTheme.severityColors.light.container
    Box(modifier = Modifier.size(HERO_ILLUSTRATION_SIZE), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRings(ringColor = ringColor, dashedRingColor = dashedRingColor)
            drawSeismogram(color = ringColor)
            drawRingDots(primaryColor = ringColor, accentColor = accentColor)
        }
        Box(
            modifier = Modifier
                .size(HERO_CENTER_SIZE)
                .shadow(elevation = HERO_CENTER_ELEVATION, shape = CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(HERO_ICON_SIZE),
            )
        }
    }
}

private fun DrawScope.drawRings(ringColor: Color, dashedRingColor: Color) {
    val radius: Float = size.minDimension / 2
    val dashLength: Float = RING_DASH_LENGTH.toPx()
    drawCircle(
        color = dashedRingColor,
        radius = radius * OUTER_RING_FRACTION,
        style = Stroke(
            width = RING_STROKE_WIDTH.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, dashLength)),
        ),
    )
    drawCircle(
        color = ringColor.copy(alpha = MIDDLE_RING_ALPHA),
        radius = radius * MIDDLE_RING_FRACTION,
        style = Stroke(width = RING_STROKE_WIDTH.toPx()),
    )
    drawCircle(
        color = ringColor.copy(alpha = INNER_RING_ALPHA),
        radius = radius * INNER_RING_FRACTION,
        style = Stroke(width = RING_STROKE_WIDTH.toPx()),
    )
}

private fun DrawScope.drawSeismogram(color: Color) {
    val width: Float = size.width
    val centerY: Float = size.height / 2
    val path = Path().apply {
        moveTo(width * SEISMOGRAM_START_FRACTION, centerY)
        lineTo(width * SEISMOGRAM_SPIKE_START_FRACTION, centerY)
        lineTo(width * SEISMOGRAM_SPIKE_PEAK_FRACTION, centerY - width * SEISMOGRAM_SPIKE_HEIGHT_FRACTION)
        lineTo(width * SEISMOGRAM_SPIKE_END_FRACTION, centerY)
        lineTo(width * SEISMOGRAM_END_FRACTION, centerY)
    }
    drawPath(path = path, color = color, style = Stroke(width = SEISMOGRAM_STROKE_WIDTH.toPx()))
}

private fun DrawScope.drawRingDots(primaryColor: Color, accentColor: Color) {
    val ringRadius: Float = size.minDimension / 2 * OUTER_RING_FRACTION
    val dotRadius: Float = RING_DOT_RADIUS.toPx()
    drawCircle(
        color = primaryColor.copy(alpha = MIDDLE_RING_ALPHA),
        radius = dotRadius,
        center = pointOnCircle(ringRadius, PRIMARY_DOT_ANGLE_DEGREES),
    )
    drawCircle(
        color = accentColor,
        radius = dotRadius * ACCENT_DOT_SCALE,
        center = pointOnCircle(ringRadius, ACCENT_DOT_ANGLE_DEGREES),
    )
}

private fun DrawScope.pointOnCircle(radius: Float, angleDegrees: Float): Offset {
    val angle: Double = Math.toRadians(angleDegrees.toDouble())
    return center + Offset(x = (radius * cos(angle)).toFloat(), y = (radius * sin(angle)).toFloat())
}

@Composable
internal fun OnboardingFeatureCard(icon: Painter, title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(FEATURE_ICON_CONTAINER_SIZE)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = FEATURE_ICON_CONTAINER_ALPHA),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val HERO_ILLUSTRATION_SIZE: Dp = 200.dp
private val HERO_CENTER_SIZE: Dp = 64.dp
private val HERO_CENTER_ELEVATION: Dp = 6.dp
private val HERO_ICON_SIZE: Dp = 32.dp
private val RING_STROKE_WIDTH: Dp = 1.5.dp
private val RING_DASH_LENGTH: Dp = 4.dp
private val RING_DOT_RADIUS: Dp = 5.dp
private val SEISMOGRAM_STROKE_WIDTH: Dp = 2.dp
private val FEATURE_ICON_CONTAINER_SIZE: Dp = 44.dp
private const val FEATURE_ICON_CONTAINER_ALPHA: Float = 0.12f
private const val OUTER_RING_FRACTION: Float = 0.96f
private const val MIDDLE_RING_FRACTION: Float = 0.74f
private const val INNER_RING_FRACTION: Float = 0.52f
private const val MIDDLE_RING_ALPHA: Float = 0.3f
private const val INNER_RING_ALPHA: Float = 0.5f
private const val SEISMOGRAM_START_FRACTION: Float = 0.12f
private const val SEISMOGRAM_SPIKE_START_FRACTION: Float = 0.28f
private const val SEISMOGRAM_SPIKE_PEAK_FRACTION: Float = 0.33f
private const val SEISMOGRAM_SPIKE_END_FRACTION: Float = 0.37f
private const val SEISMOGRAM_END_FRACTION: Float = 0.88f
private const val SEISMOGRAM_SPIKE_HEIGHT_FRACTION: Float = 0.08f
private const val PRIMARY_DOT_ANGLE_DEGREES: Float = -55f
private const val ACCENT_DOT_ANGLE_DEGREES: Float = 125f
private const val ACCENT_DOT_SCALE: Float = 1.3f
