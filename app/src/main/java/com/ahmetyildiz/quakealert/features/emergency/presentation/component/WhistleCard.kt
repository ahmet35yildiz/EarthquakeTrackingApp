package com.ahmetyildiz.quakealert.features.emergency.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.ToolState

@Composable
fun WhistleCard(state: ToolState, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    EmergencyToolCard(
        title = stringResource(R.string.whistle_title),
        icon = painterResource(R.drawable.ic_notifications),
        description = stringResource(R.string.whistle_description),
        failureMessage = stringResource(R.string.whistle_failed),
        state = state,
        onToggle = onToggle,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun WhistleCardPreview() {
    QuakeAlertTheme {
        Surface {
            WhistleCard(state = ToolState(), onToggle = {}, modifier = Modifier.padding(Spacing.large))
        }
    }
}
