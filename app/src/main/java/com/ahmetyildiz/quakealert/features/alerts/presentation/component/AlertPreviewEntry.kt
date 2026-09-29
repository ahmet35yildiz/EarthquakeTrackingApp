package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertPreviewUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertPreviewViewModel

@Composable
fun AlertPreviewEntry(
    threshold: Double,
    area: AlertArea?,
    modifier: Modifier = Modifier,
    viewModel: AlertPreviewViewModel = hiltViewModel(),
) {
    val choice: AlertChoice? = area?.let { AlertChoice(magnitudeThreshold = threshold, area = it) }
    LaunchedEffect(choice) { viewModel.onChoiceChanged(choice) }
    val state: AlertPreviewUiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlertPreviewCard(state = state, modifier = modifier)
}
