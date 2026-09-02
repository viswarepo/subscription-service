package com.sms.sub.config;

import com.sms.sub.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Illustrative scheduled billing run. Off by default (see
 * subscription.billing.scheduler.enabled in application.yml) — a real
 * deployment would more likely run this as a dedicated batch job or
 * queue-driven worker rather than an in-process cron in the API pod, so it
 * can be scaled, retried, and monitored independently. Enable it here only
 * for local demos.
 */
@Component
@ConditionalOnProperty(name = "subscription.billing.scheduler.enabled", havingValue = "true")
public class BillingCycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillingCycleScheduler.class);

    private final SubscriptionService subscriptionService;

    public BillingCycleScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(cron = "${subscription.billing.scheduler.cron:0 0 2 * * *}")
    public void run() {
        var result = subscriptionService.processBillingCycle(Instant.now());
        log.info("Billing cycle processed: renewed={}, canceledAtPeriodEnd={}, expiredFromDunning={}, trialsExpired={}",
                result.renewed(), result.canceledAtPeriodEnd(), result.expiredFromDunning(), result.trialsExpired());
    }
}
