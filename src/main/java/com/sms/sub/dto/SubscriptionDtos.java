package com.sms.sub.dto;

import com.sms.sub.domain.BillingCycle;
import com.sms.sub.domain.CurrencyCode;
import com.sms.sub.domain.SubscriptionEventType;
import com.sms.sub.domain.SubscriptionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class SubscriptionDtos {

    private SubscriptionDtos() {
    }

    public record CreateSubscriptionRequest(
            @NotNull String customerId,
            @NotBlank String productCode,
            @NotBlank String planCode,
            @Min(1) int planVersion,
            @NotNull CurrencyCode currency,
            @NotNull BillingCycle billingCycle,
            @NotNull @Min(0) BigDecimal unitAmount,
            @Min(0) int trialDays
    ) {
    }

    public record ChangePlanRequest(
            @NotBlank String planCode,
            @Min(1) int planVersion,
            @NotNull @Min(0) BigDecimal unitAmount,
            @NotNull BillingCycle billingCycle,
            boolean immediate
    ) {
    }

    public record CancelRequest(
            boolean atPeriodEnd
    ) {
    }

    public record SubscriptionResponse(
            String id,
            String organizationId,
            String customerId,
            String customerName,
            String productCode,
            String planCode,
            int planVersion,
            SubscriptionStatus status,
            CurrencyCode currency,
            BillingCycle billingCycle,
            BigDecimal unitAmount,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            Instant trialEnd,
            boolean cancelAtPeriodEnd,
            Instant canceledAt,
            Instant endedAt,
            String scheduledPlanCode,
            Integer scheduledPlanVersion,
            BigDecimal scheduledUnitAmount,
            BillingCycle scheduledBillingCycle,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record SubscriptionEventResponse(
            SubscriptionEventType type,
            Instant occurredAt,
            String notes
    ) {
    }

    public record BillingRunResponse(
            int renewed,
            int canceledAtPeriodEnd,
            int expiredFromDunning,
            int trialsExpired
    ) {
    }
}
