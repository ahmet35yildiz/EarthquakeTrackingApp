package com.ahmetyildiz.quakealert.core.analytics

class FakeAnalyticsTracker : AnalyticsTracker {

    val events: MutableList<AnalyticsEvent> = mutableListOf()

    override fun track(event: AnalyticsEvent) {
        events += event
    }
}
