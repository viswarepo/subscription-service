package com.sms.sub.domain;

/**
 * Every meaningful state change on a Subscription is recorded as a SubscriptionEvent
 * of one of these types, giving a full audit trail independent of the current
 * row state (useful for support, analytics, and dunning reporting).
 */
public enum SubscriptionEventType {
    CREATED,
    TRIAL_CONVERTED,
    RENEWED,
    PLAN_CHANGE_SCHEDULED,
    PLAN_CHANGED,
    PAYMENT_FAILED,
    PAYMENT_SUCCEEDED,
    CANCELLATION_SCHEDULED,
    CANCELED,
    REACTIVATED,
    PAUSED,
    RESUMED,
    EXPIRED
}
