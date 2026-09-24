package com.sms.sub.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Published after the master Subscription record is committed. Consumed by
 * billing-payment-service to create its own local Subscription (and, if
 * needed, Customer) row for invoicing/payment purposes.
 *
 * billingCycle is a plain String, not a shared enum type - subscription-service
 * and billing-payment-service each own their own separate BillingCycle enum,
 * and a shared Java type here would create a compile-time coupling between
 * two independently deployed services that doesn't actually exist at the
 * wire level. The consumer maps this string onto its own enum. VERIFY the
 * exact constant names subscription-service's own BillingCycle enum uses
 * (e.g. "ANNUAL" vs "YEARLY") match what billing-service's consumer expects
 * to parse, or map between them explicitly on the consumer side.
 *
 * eventId exists for consumer-side idempotency: Kafka's default delivery
 * guarantee is at-least-once, so the same event can arrive more than once.
 * The consumer should track processed eventIds (or otherwise make its
 * handling of this event idempotent) rather than assume exactly-once.
 */
public record SubscriptionCreatedEvent(
        UUID eventId,
        String subscriptionId,
        String organizationId,
        String customerId,
        String customerEmail,
        String customerName,
        String productCode,
        String planCode,
        int planVersion,
        BigDecimal unitAmount,
        String currency,
        String billingCycle,
        int trialDays,
        Instant occurredAt
) {
    public static SubscriptionCreatedEvent of(
            String subscriptionId,
            String organizationId,
            String customerId,
            String customerEmail,
            String customerName,
            String productCode,
            String planCode,
            int planVersion,
            BigDecimal unitAmount,
            String currency,
            String billingCycle,
            int trialDays) {
        return new SubscriptionCreatedEvent(
                UUID.randomUUID(),
                subscriptionId,
                organizationId,
                customerId,
                customerEmail,
                customerName,
                productCode,
                planCode,
                planVersion,
                unitAmount,
                currency,
                billingCycle,
                trialDays,
                Instant.now());
    }
}
