package com.ahmetyildiz.quakealert.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity

@Immutable
data class SeverityColor(
    val container: Color,
    val onContainer: Color,
)

@Immutable
data class SeverityColors(
    val minor: SeverityColor,
    val light: SeverityColor,
    val moderate: SeverityColor,
    val strong: SeverityColor,
    val major: SeverityColor,
) {
    fun colorFor(severity: MagnitudeSeverity): SeverityColor = when (severity) {
        MagnitudeSeverity.MINOR -> minor
        MagnitudeSeverity.LIGHT -> light
        MagnitudeSeverity.MODERATE -> moderate
        MagnitudeSeverity.STRONG -> strong
        MagnitudeSeverity.MAJOR -> major
    }
}

internal val LightSeverityColors: SeverityColors = SeverityColors(
    minor = SeverityColor(container = Color(0xFF006874), onContainer = Color(0xFFFFFFFF)),
    light = SeverityColor(container = Color(0xFF765A00), onContainer = Color(0xFFFFFFFF)),
    moderate = SeverityColor(container = Color(0xFF944B00), onContainer = Color(0xFFFFFFFF)),
    strong = SeverityColor(container = Color(0xFFBA1A1A), onContainer = Color(0xFFFFFFFF)),
    major = SeverityColor(container = Color(0xFF680016), onContainer = Color(0xFFFFFFFF)),
)

internal val DarkSeverityColors: SeverityColors = SeverityColors(
    minor = SeverityColor(container = Color(0xFF4FD8EB), onContainer = Color(0xFF00363D)),
    light = SeverityColor(container = Color(0xFFE9C349), onContainer = Color(0xFF3C2F00)),
    moderate = SeverityColor(container = Color(0xFFFFB688), onContainer = Color(0xFF4F2500)),
    strong = SeverityColor(container = Color(0xFFFFB4AB), onContainer = Color(0xFF690005)),
    major = SeverityColor(container = Color(0xFFA8353F), onContainer = Color(0xFFFFEDEC)),
)

val LocalSeverityColors = staticCompositionLocalOf { LightSeverityColors }
