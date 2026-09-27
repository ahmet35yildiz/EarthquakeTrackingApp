package com.ahmetyildiz.quakealert.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

private val OPTION_MIN_HEIGHT: Dp = 48.dp

data class SingleChoiceDialogContent<T>(
    val title: String,
    val options: List<T>,
    val selected: T,
    val optionLabel: @Composable (T) -> String,
)

@Composable
fun <T> SingleChoiceDialog(
    content: SingleChoiceDialogContent<T>,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = content.title) },
        text = {
            Column(modifier = Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                content.options.forEach { option ->
                    ChoiceOption(
                        label = content.optionLabel(option),
                        isSelected = option == content.selected,
                        onClick = { onSelected(option) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun ChoiceOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = OPTION_MIN_HEIGHT)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@PreviewLightDark
@Composable
private fun SingleChoiceDialogPreview() {
    QuakeAlertTheme {
        SingleChoiceDialog(
            content = SingleChoiceDialogContent(
                title = "Choose language",
                options = listOf("System default", "English", "Türkçe"),
                selected = "English",
                optionLabel = { it },
            ),
            onSelected = {},
            onDismiss = {},
        )
    }
}
