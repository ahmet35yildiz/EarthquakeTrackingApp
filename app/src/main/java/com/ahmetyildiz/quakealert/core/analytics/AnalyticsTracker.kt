package com.ahmetyildiz.quakealert.core.analytics

/** Records product analytics events. A remote service (e.g. Firebase) can be added as another implementation. */
interface AnalyticsTracker {

    /** Records [event] in the background; returns immediately and never throws. */
    fun track(event: AnalyticsEvent)
}
