package com.sms.sub.service;

import com.sms.sub.domain.BillingCycle;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Small pure-function helper for advancing a billing period boundary by one
 * cycle. Calendar-aware (a MONTHLY period from Jan 31 lands on the correct
 * end-of-month day via java.time), rather than a fixed 30-day Duration.
 */
public final class BillingCycleCalculator {

    private BillingCycleCalculator() {
    }

    public static Instant addOneCycle(Instant from, BillingCycle cycle) {
        ZonedDateTime start = from.atZone(ZoneOffset.UTC);
        ZonedDateTime next = switch (cycle) {
            case WEEKLY -> start.plusWeeks(1);
            case MONTHLY -> start.plusMonths(1);
            case QUARTERLY -> start.plusMonths(3);
            case ANNUAL -> start.plusYears(1);
        };
        return next.toInstant();
    }

    public static Instant addTrialDays(Instant from, int trialDays) {
        return from.atZone(ZoneOffset.UTC).plusDays(trialDays).toInstant();
    }
}
