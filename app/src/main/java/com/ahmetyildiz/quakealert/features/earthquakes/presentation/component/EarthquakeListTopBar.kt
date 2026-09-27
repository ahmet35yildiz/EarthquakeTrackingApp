package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.format.relativeTimeText
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListTopBar(
    lastRefreshedAt: Instant?,
    now: Instant,
    isRefreshEnabled: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .heightIn(min = TopAppBarDefaults.TopAppBarExpandedHeight)
                .padding(start = Spacing.large, end = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = Spacing.small)) {
                ScreenTitle(
                    text = stringResource(R.string.tab_earthquakes),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = subtitleText(lastRefreshedAt, now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRefresh, enabled = isRefreshEnabled) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = stringResource(R.string.action_refresh))
            }
        }
    }
}

@Composable
private fun subtitleText(lastRefreshedAt: Instant?, now: Instant): String {
    val days: Int = EarthquakesConfig.RECENT_PERIOD.toDays().toInt()
    val minMagnitude: String = stringResource(R.string.magnitude_value, EarthquakesConfig.RECENT_MIN_MAGNITUDE)
    val scope: String = pluralStringResource(R.plurals.earthquake_list_scope, days, days, minMagnitude)
    if (lastRefreshedAt == null) return scope
    val updated: String = stringResource(R.string.earthquake_list_updated, relativeTimeText(lastRefreshedAt, now))
    return "$scope · $updated"
}
