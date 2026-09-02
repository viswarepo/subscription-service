package com.sms.sub.config;

import com.sms.sub.domain.BillingCycle;
import com.sms.sub.domain.CurrencyCode;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.Subscription;
import com.sms.sub.service.SubscriptionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Populates the in-memory H2 database with sample customers and subscriptions
 * for two different organizations (tenants) on startup, so the API is
 * immediately explorable — and so it's easy to see that org_acme's data is
 * invisible when querying as org_globex. Not intended for production use.
 */
//@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(SubscriptionService subscriptionService) {
        return args -> {

            // ---- Tenant: org_acme ----
            Customer amara = subscriptionService.createCustomer("f1b419af-ea20-4f38-b3fe-5d38bfacdfde",  "amara@example.com", "Amara Okafor");
            Customer devon = subscriptionService.createCustomer("f1b419af-ea20-4f38-b3fe-5d38bfacdfde",  "devon@example.com", "Devon Blake");
            Customer priya = subscriptionService.createCustomer("f1b419af-ea20-4f38-b3fe-5d38bfacdfde",  "priya@example.com", "Priya Sharma");

            // Amara: active PRO subscription, monthly, no trial.
            subscriptionService.createSubscription(
                    "f1b419af-ea20-4f38-b3fe-5d38bfacdfde", amara.getId(),"ACME_CLOUD", "PRO", 2,
                    CurrencyCode.USD, BillingCycle.MONTHLY, new BigDecimal("35.00"), 0);

            // Devon: currently in a 14-day trial of PRO.
            subscriptionService.createSubscription(
                    "f1b419af-ea20-4f38-b3fe-5d38bfacdfde", devon.getId(), "ACME_CLOUD", "PRO", 2,
                    CurrencyCode.USD, BillingCycle.MONTHLY, new BigDecimal("35.00"), 14);

            // Priya: active FREE plan, with an upgrade to PRO scheduled for her next renewal.
            Subscription priyaFree = subscriptionService.createSubscription(
                    "f1b419af-ea20-4f38-b3fe-5d38bfacdfde", priya.getId(), "ACME_CLOUD", "FREE", 1,
                    CurrencyCode.USD, BillingCycle.MONTHLY, BigDecimal.ZERO, 0);
            subscriptionService.changePlan(
                    "f1b419af-ea20-4f38-b3fe-5d38bfacdfde", priyaFree.getId(), "PRO", 2, new BigDecimal("35.00"), BillingCycle.MONTHLY, false);

            // ---- Tenant: org_globex (kept separate to demonstrate tenant isolation) ----
            Customer kenji = subscriptionService.createCustomer("org_globex",  "kenji@example.com", "Kenji Watanabe");
            subscriptionService.createSubscription(
                    "f1b419af-ea20-4f38-b3fe-5d38bfacdfde", kenji.getId(), "GLOBEX_SUITE", "STANDARD", 1,
                    CurrencyCode.EUR, BillingCycle.ANNUAL, new BigDecimal("240.00"), 0);

            System.out.println("Seeded 2 organizations (org_acme: 3 customers/subscriptions, org_globex: 1). "
                    + "Send requests with header X-Organization-Id: org_acme (or org_globex) to see tenant-scoped data. "
                    + "Try POST /api/v1/subscriptions/process-billing-cycle?asOf=<future ISO instant> "
                    + "to see Devon's trial expire and Priya's upgrade apply at renewal.");
        };
    }
}
