package com.ahmetyildiz.quakealert.features.eventlog.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.eventlog.domain.AlertMetricsConfig
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.AlertMetrics
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.MetricRatio

@Composable
fun AlertMetricsCard(
    metrics: AlertMetrics,
    modifier: Modifier = Modifier,
) {
    val windowHours: Int = metrics.optOutWindow.toHours().toInt()
    SectionCard(
        title = stringResource(R.string.metrics_title),
        icon = painterResource(R.drawable.ic_notifications),
        modifier = modifier,
    ) {
        MetricRow(label = stringResource(R.string.metrics_setup_completed), ratio = metrics.setupCompletion)
        HorizontalDivider()
        MetricRow(label = stringResource(R.string.metrics_alerts_opened), ratio = metrics.notificationOpens)
        HorizontalDivider()
        MetricRow(
            label = stringResource(R.string.metrics_useful_answers),
            ratio = metrics.usefulAnswers,
            detail = stringResource(
                R.string.metrics_useful_answers_detail,
                metrics.answeredOpens.count,
                metrics.answeredOpens.total,
            ),
        )
        HorizontalDivider()
        MetricRow(
            label = stringResource(R.string.metrics_opt_out, windowHours),
            ratio = metrics.notificationsFollowedByOptOut,
            detail = stringResource(
                R.string.metrics_opt_out_detail,
                metrics.alertsTurnedOffAfterNotification,
                metrics.thresholdsRaisedAfterNotification,
            ),
        )
    }
}

@Composable
private fun MetricRow(label: String, ratio: MetricRatio, detail: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(text = ratioText(ratio), style = MaterialTheme.typography.titleSmall)
        }
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ratioText(ratio: MetricRatio): String {
    val percent: Int = ratio.percent ?: return stringResource(R.string.metrics_ratio_without_percent, ratio.count, ratio.total)
    return stringResource(R.string.metrics_ratio, ratio.count, ratio.total, percent)
}

@PreviewLightDark
@Composable
private fun AlertMetricsCardPreview() {
    QuakeAlertTheme {
        Surface {
            AlertMetricsCard(
                metrics = AlertMetrics(
                    setupCompletion = MetricRatio(count = 1, total = 1),
                    notificationOpens = MetricRatio(count = 3, total = 5),
                    answeredOpens = MetricRatio(count = 2, total = 3),
                    usefulAnswers = MetricRatio(count = 1, total = 2),
                    notificationsFollowedByOptOut = MetricRatio(count = 1, total = 5),
                    alertsTurnedOffAfterNotification = 0,
                    thresholdsRaisedAfterNotification = 1,
                    optOutWindow = AlertMetricsConfig.OPT_OUT_WINDOW,
                ),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
