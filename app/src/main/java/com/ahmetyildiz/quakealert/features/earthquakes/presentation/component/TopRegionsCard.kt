package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.format.localizedPlace
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionCount

@Composable
fun TopRegionsCard(topRegions: List<RegionCount>, modifier: Modifier = Modifier) {
    SectionCard(
        title = stringResource(R.string.statistics_regions_title),
        icon = rememberVectorPainter(Icons.Rounded.Place),
        modifier = modifier,
    ) {
        topRegions.forEachIndexed { index, regionCount ->
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                Text(
                    text = formatWholeNumber((index + 1).toDouble()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = localizedPlace(regionCount.region),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(text = formatWholeNumber(regionCount.count.toDouble()), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
