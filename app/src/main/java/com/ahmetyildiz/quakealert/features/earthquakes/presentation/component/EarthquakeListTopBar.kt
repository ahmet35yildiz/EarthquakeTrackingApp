package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.format.relativeTimeText
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
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                ScreenTitle(text = stringResource(R.string.tab_earthquakes))
                Text(
                    text = subtitleText(lastRefreshedAt, now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh, enabled = isRefreshEnabled) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = stringResource(R.string.action_refresh))
            }
        },
    )
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
