package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.time.RelativeTime
import java.time.Instant

@Composable
fun relativeTimeText(time: Instant, now: Instant): String =
    when (val relativeTime: RelativeTime = RelativeTime.between(time, now)) {
        RelativeTime.JustNow -> stringResource(R.string.relative_time_just_now)
        is RelativeTime.MinutesAgo -> relativeTimeWithCount(R.plurals.relative_time_minutes_ago, relativeTime.minutes)
        is RelativeTime.HoursAgo -> relativeTimeWithCount(R.plurals.relative_time_hours_ago, relativeTime.hours)
        is RelativeTime.DaysAgo -> relativeTimeWithCount(R.plurals.relative_time_days_ago, relativeTime.days)
    }

@Composable
private fun relativeTimeWithCount(pluralsRes: Int, count: Long): String =
    pluralStringResource(pluralsRes, count.toInt(), count.toInt())
