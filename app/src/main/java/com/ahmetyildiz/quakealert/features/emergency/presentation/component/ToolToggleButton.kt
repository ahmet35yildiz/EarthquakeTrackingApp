package com.ahmetyildiz.quakealert.features.emergency.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R

@Composable
fun ToolToggleButton(isOn: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors: ButtonColors = if (isOn) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    } else {
        ButtonDefaults.buttonColors()
    }
    Button(onClick = onToggle, colors = colors, modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(if (isOn) R.string.action_stop else R.string.action_start))
    }
}
