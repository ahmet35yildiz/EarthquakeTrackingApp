package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun AlertFeedbackCard(
    isAnswered: Boolean,
    onAnswer: (isUseful: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.medium)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            if (isAnswered) {
                Text(text = stringResource(R.string.alert_feedback_thanks), style = MaterialTheme.typography.bodyMedium)
            } else {
                FeedbackQuestion(onAnswer = onAnswer)
            }
        }
    }
}

@Composable
private fun FeedbackQuestion(onAnswer: (isUseful: Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(
            text = stringResource(R.string.alert_feedback_question),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(onClick = { onAnswer(true) }) { Text(text = stringResource(R.string.action_yes)) }
        OutlinedButton(onClick = { onAnswer(false) }) { Text(text = stringResource(R.string.action_no)) }
    }
}

@PreviewLightDark
@Composable
private fun AlertFeedbackCardPreview() {
    QuakeAlertTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                AlertFeedbackCard(isAnswered = false, onAnswer = {})
                AlertFeedbackCard(isAnswered = true, onAnswer = {})
            }
        }
    }
}
