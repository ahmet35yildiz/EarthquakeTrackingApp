package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.format.rememberFittingTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.emergency.presentation.component.SafetyGuideItem
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.SafetyGuideSection
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.SafetyGuideViewModel

@Composable
fun SafetyGuideEntry(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SafetyGuideViewModel = hiltViewModel(),
) {
    val selectedSection: SafetyGuideSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    SafetyGuideScreen(
        selectedSection = selectedSection,
        onSectionSelected = viewModel::onSectionSelected,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyGuideScreen(
    selectedSection: SafetyGuideSection,
    onSectionSelected: (SafetyGuideSection) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { ScreenTitle(text = stringResource(R.string.safety_guide_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SafetyGuideTabs(selectedSection = selectedSection, onSectionSelected = onSectionSelected)
            key(selectedSection) {
                SafetyGuideItems(section = selectedSection, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SafetyGuideTabs(selectedSection: SafetyGuideSection, onSectionSelected: (SafetyGuideSection) -> Unit) {
    BoxWithConstraints {
        val titles: List<String> = SafetyGuideSection.entries.map { stringResource(it.titleRes) }
        val titleStyle: TextStyle = rememberFittingTextStyle(
            lines = titles.flatMap { it.lines() },
            maxWidth = maxWidth / titles.size - TAB_TEXT_HORIZONTAL_PADDING * 2,
            style = MaterialTheme.typography.titleSmall,
        )
        PrimaryTabRow(selectedTabIndex = selectedSection.ordinal) {
            SafetyGuideSection.entries.forEachIndexed { index, section ->
                Tab(
                    selected = section == selectedSection,
                    onClick = { onSectionSelected(section) },
                    text = { Text(text = titles[index], style = LocalTextStyle.current.merge(titleStyle)) },
                )
            }
        }
    }
}

private val TAB_TEXT_HORIZONTAL_PADDING: Dp = 16.dp

@Composable
private fun SafetyGuideItems(section: SafetyGuideSection, modifier: Modifier = Modifier) {
    val items: Array<String> = stringArrayResource(section.itemsRes)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenMargin, vertical = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        items.forEachIndexed { index, item -> SafetyGuideItem(number = index + 1, text = item) }
        Text(
            text = stringResource(R.string.safety_guide_source),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.small),
        )
    }
}

@get:StringRes
private val SafetyGuideSection.titleRes: Int
    get() = when (this) {
        SafetyGuideSection.BEFORE -> R.string.safety_guide_section_before
        SafetyGuideSection.DURING -> R.string.safety_guide_section_during
        SafetyGuideSection.AFTER -> R.string.safety_guide_section_after
    }

@get:ArrayRes
private val SafetyGuideSection.itemsRes: Int
    get() = when (this) {
        SafetyGuideSection.BEFORE -> R.array.safety_guide_before_items
        SafetyGuideSection.DURING -> R.array.safety_guide_during_items
        SafetyGuideSection.AFTER -> R.array.safety_guide_after_items
    }

@PreviewLightDark
@Composable
private fun SafetyGuideScreenPreview() {
    QuakeAlertTheme {
        Surface {
            SafetyGuideScreen(selectedSection = SafetyGuideSection.DURING, onSectionSelected = {}, onBack = {})
        }
    }
}
