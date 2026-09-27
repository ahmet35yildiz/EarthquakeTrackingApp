package com.ahmetyildiz.quakealert.features.settings.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.appearance.ThemeMode
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun ThemeSection(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPickerOpen: Boolean by rememberSaveable { mutableStateOf(false) }
    SectionCard(
        title = stringResource(R.string.settings_theme_title),
        icon = rememberVectorPainter(Icons.Rounded.Edit),
        modifier = modifier,
    ) {
        SelectedValueRow(
            value = stringResource(selectedMode.labelRes),
            onClickLabel = stringResource(R.string.settings_theme_change),
            onClick = { isPickerOpen = true },
        )
    }
    if (isPickerOpen) {
        SingleChoiceDialog(
            content = SingleChoiceDialogContent(
                title = stringResource(R.string.settings_theme_dialog_title),
                options = ThemeMode.entries,
                selected = selectedMode,
                optionLabel = { stringResource(it.labelRes) },
            ),
            onSelected = { mode ->
                isPickerOpen = false
                onModeSelected(mode)
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

@PreviewLightDark
@Composable
private fun ThemeSectionPreview() {
    QuakeAlertTheme {
        Surface {
            ThemeSection(selectedMode = ThemeMode.SYSTEM, onModeSelected = {}, modifier = Modifier.padding(Spacing.large))
        }
    }
}
