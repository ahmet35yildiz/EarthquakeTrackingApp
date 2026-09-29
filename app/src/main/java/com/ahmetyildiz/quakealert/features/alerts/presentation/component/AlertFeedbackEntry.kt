package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertFeedbackViewModel

@Composable
fun AlertFeedbackEntry(
    eventId: String,
    modifier: Modifier = Modifier,
    viewModel: AlertFeedbackViewModel = hiltViewModel(),
) {
    val isAnswered: Boolean by viewModel.isAnswered.collectAsStateWithLifecycle()
    AlertFeedbackCard(
        isAnswered = isAnswered,
        onAnswer = { isUseful -> viewModel.onAnswered(eventId = eventId, isUseful = isUseful) },
        modifier = modifier,
    )
}
