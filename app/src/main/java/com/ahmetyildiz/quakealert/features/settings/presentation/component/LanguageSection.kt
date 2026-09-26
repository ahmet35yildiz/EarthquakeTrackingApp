package com.ahmetyildiz.quakealert.features.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun LanguageSection(
    languages: List<AppLanguage>,
    selectedLanguage: AppLanguage?,
    onLanguageSelected: (AppLanguage?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPickerOpen: Boolean by rememberSaveable { mutableStateOf(false) }
    SectionCard(
        title = stringResource(R.string.settings_language_title),
        icon = painterResource(R.drawable.ic_language),
        modifier = modifier,
    ) {
        SelectedLanguageRow(selectedLanguage = selectedLanguage, onClick = { isPickerOpen = true })
    }
    if (isPickerOpen) {
        LanguagePickerDialog(
            languages = languages,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                isPickerOpen = false
                onLanguageSelected(language)
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@Composable
fun languageLabel(language: AppLanguage?): String =
    language?.nativeName ?: stringResource(R.string.settings_language_system_default)

@Composable
private fun SelectedLanguageRow(selectedLanguage: AppLanguage?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    onClickLabel = stringResource(R.string.settings_language_change),
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = languageLabel(selectedLanguage),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LanguageSectionPreview() {
    QuakeAlertTheme {
        Surface {
            LanguageSection(
                languages = listOf(AppLanguage("en"), AppLanguage("tr")),
                selectedLanguage = AppLanguage("tr"),
                onLanguageSelected = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
