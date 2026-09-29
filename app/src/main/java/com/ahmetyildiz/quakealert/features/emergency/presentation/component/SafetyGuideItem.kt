package com.ahmetyildiz.quakealert.features.emergency.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

private val NumberBadgeSize: Dp = 28.dp

@Composable
fun SafetyGuideItem(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(NumberBadgeSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = formatWholeNumber(number.toDouble()), style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(top = Spacing.extraSmall),
        )
    }
}

@PreviewLightDark
@Composable
private fun SafetyGuideItemPreview() {
    QuakeAlertTheme {
        Surface {
            SafetyGuideItem(
                number = 1,
                text = "Drop, Cover, Hold On: get down, protect your head and neck, and hold on until the shaking stops.",
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
