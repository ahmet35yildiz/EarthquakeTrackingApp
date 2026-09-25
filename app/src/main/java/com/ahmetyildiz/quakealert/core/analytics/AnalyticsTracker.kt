package com.ahmetyildiz.quakealert.core.analytics

interface AnalyticsTracker {

    fun track(event: AnalyticsEvent)
}
