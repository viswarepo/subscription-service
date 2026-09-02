package com.sms.sub.controller;

import com.sms.sub.domain.Customer;
import com.sms.sub.domain.Subscription;
import com.sms.sub.domain.SubscriptionEvent;
import com.sms.sub.service.SubscriptionService.BillingRunResult;
import com.sms.sub.dto.CustomerDtos.CustomerResponse;
import com.sms.sub.dto.SubscriptionDtos.BillingRunResponse;
import com.sms.sub.dto.SubscriptionDtos.SubscriptionEventResponse;
import com.sms.sub.dto.SubscriptionDtos.SubscriptionResponse;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getOrganizationId(),
                customer.getExternalId(),
                customer.getEmail(),
                customer.getName(),
                customer.getCreatedAt()
        );
    }

    public SubscriptionResponse toResponse(Subscription sub) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getOrganizationId(),
                sub.getCustomer().getId(),
                sub.getProductCode(),
                sub.getPlanCode(),
                sub.getPlanVersion(),
                sub.getStatus(),
                sub.getCurrency(),
                sub.getBillingCycle(),
                sub.getUnitAmount(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd(),
                sub.getTrialEnd(),
                sub.isCancelAtPeriodEnd(),
                sub.getCanceledAt(),
                sub.getEndedAt(),
                sub.getScheduledPlanCode(),
                sub.getScheduledPlanVersion(),
                sub.getScheduledUnitAmount(),
                sub.getScheduledBillingCycle(),
                sub.getCreatedAt(),
                sub.getUpdatedAt()
        );
    }

    public SubscriptionEventResponse toResponse(SubscriptionEvent event) {
        return new SubscriptionEventResponse(event.getType(), event.getOccurredAt(), event.getNotes());
    }

    public BillingRunResponse toResponse(BillingRunResult result) {
        return new BillingRunResponse(
                result.renewed(), result.canceledAtPeriodEnd(), result.expiredFromDunning(), result.trialsExpired());
    }
}
