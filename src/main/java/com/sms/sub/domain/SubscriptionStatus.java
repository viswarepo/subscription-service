package com.sms.sub.domain;

/**
 * Lifecycle states for a subscription.
 *
 * TRIALING  -> in a free trial period, not yet charged
 * ACTIVE    -> paid and in good standing
 * PAST_DUE  -> a renewal charge failed; in a grace/dunning period
 * PAUSED    -> temporarily suspended by the customer or an operator, not billing
 * CANCELED  -> ended deliberately (immediately or at period end)
 * EXPIRED   -> ended involuntarily (dunning exhausted, or trial never converted)
 */
public enum SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    PAUSED,
    CANCELED,
    EXPIRED
}
