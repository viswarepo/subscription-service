package com.sms.sub.controller;

import com.sms.sub.domain.Subscription;
import com.sms.sub.service.SubscriptionService;
import com.sms.sub.dto.SubscriptionDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Customer subscriptions: creation, trials, renewals, plan changes, cancellation")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper mapper;

    public SubscriptionController(SubscriptionService subscriptionService, SubscriptionMapper mapper) {
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Create a subscription",
            description = "Starts TRIALING if trialDays > 0, otherwise ACTIVE immediately. Fails with 409 "
                    + "if the customer already has a live (non-terminal) subscription to this productCode.")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription sub = subscriptionService.createSubscription(
                organizationId, request.customerId(), request.productCode(), request.planCode(), request.planVersion(),
                request.currency(), request.billingCycle(), request.unitAmount(), request.trialDays());
        return mapper.toResponse(sub);
    }

    @Operation(summary = "Get a subscription by ID")
    @GetMapping("/{subscriptionId}")
    public SubscriptionResponse get(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.getSubscription(organizationId, subscriptionId));
    }

    @Operation(summary = "Get a subscription by ID")
    @GetMapping("/all")
    public List<SubscriptionResponse> getAllSubscription(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId) {
        return subscriptionService.getAllSubscription(organizationId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Operation(summary = "List a customer's subscriptions")
    @GetMapping("/my/{customerId}")
    public List<SubscriptionResponse> listForCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String customerId) {
        return subscriptionService.getSubscriptionsForCustomer(organizationId, customerId).stream()
                .map(mapper::toResponse).toList();
    }

    @Operation(summary = "Get a subscription's audit trail", description = "Full history of lifecycle events, newest first.")
    @GetMapping("/{subscriptionId}/events")
    public List<SubscriptionEventResponse> events(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return subscriptionService.getEvents(organizationId, subscriptionId).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Convert a trial to paid", description = "TRIALING -> ACTIVE immediately, ahead of the scheduled trial end.")
    @PostMapping("/{subscriptionId}/convert-trial")
    public SubscriptionResponse convertTrial(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.convertTrial(organizationId, subscriptionId));
    }

    @Operation(summary = "Record a successful charge", description = "Moves TRIALING or PAST_DUE subscriptions to ACTIVE.")
    @PostMapping("/{subscriptionId}/payment-succeeded")
    public SubscriptionResponse paymentSucceeded(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.recordPaymentSuccess(organizationId, subscriptionId));
    }

    @Operation(summary = "Record a failed charge", description = "ACTIVE or TRIALING -> PAST_DUE, entering dunning.")
    @PostMapping("/{subscriptionId}/payment-failed")
    public SubscriptionResponse paymentFailed(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.recordPaymentFailure(organizationId, subscriptionId));
    }

    @Operation(
            summary = "Renew a subscription's billing period",
            description = "Normally invoked by the billing run, not directly. Advances the period; finalizes "
                    + "a scheduled cancellation instead of renewing if one is pending; applies a scheduled "
                    + "plan change if one is pending.")
    @PostMapping("/{subscriptionId}/renew")
    public SubscriptionResponse renew(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.renew(organizationId, subscriptionId));
    }

    @Operation(
            summary = "Change plan (upgrade/downgrade)",
            description = "immediate=true applies now with no proration in this sample (see README); "
                    + "immediate=false stages the change to apply automatically on the next renewal.")
    @PostMapping("/{subscriptionId}/change-plan")
    public SubscriptionResponse changePlan(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId,
            @RequestBody ChangePlanRequest request) {
        Subscription sub = subscriptionService.changePlan(
                organizationId, subscriptionId, request.planCode(), request.planVersion(),
                request.unitAmount(), request.billingCycle(), request.immediate());
        return mapper.toResponse(sub);
    }

    @Operation(
            summary = "Cancel a subscription",
            description = "atPeriodEnd=true schedules cancellation for the end of the current period (status "
                    + "unchanged until then); atPeriodEnd=false cancels immediately.")
    @PostMapping("/{subscriptionId}/cancel")
    public SubscriptionResponse cancel(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId,
            @RequestBody CancelRequest request) {
        return mapper.toResponse(subscriptionService.cancel(organizationId, subscriptionId, request.atPeriodEnd()));
    }

    @Operation(summary = "Undo a scheduled cancellation", description = "Only works while cancelAtPeriodEnd is true and the subscription hasn't ended yet. An already-CANCELED subscription cannot be reactivated — create a new one.")
    @PostMapping("/{subscriptionId}/reactivate")
    public SubscriptionResponse reactivate(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.reactivate(organizationId, subscriptionId));
    }

    @Operation(summary = "Pause a subscription", description = "ACTIVE -> PAUSED. Billing periods are not extended by pause duration in this sample.")
    @PostMapping("/{subscriptionId}/pause")
    public SubscriptionResponse pause(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.pause(organizationId, subscriptionId));
    }

    @Operation(summary = "Resume a paused subscription", description = "PAUSED -> ACTIVE.")
    @PostMapping("/{subscriptionId}/resume")
    public SubscriptionResponse resume(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return mapper.toResponse(subscriptionService.resume(organizationId, subscriptionId));
    }

    @Operation(
            summary = "Run the billing cycle now",
            description = "Manually triggers what a scheduled billing job would do, across ALL organizations: "
                    + "renews subscriptions whose period has ended, finalizes scheduled cancellations, expires "
                    + "subscriptions whose dunning period lapsed, and expires trials that never converted. This "
                    + "is an operational/admin endpoint, not tenant-scoped, so it does not require the "
                    + "X-Organization-Id header. Defaults to processing as of the current time; pass asOf to "
                    + "simulate a future billing date against seeded data.")
    @PostMapping("/process-billing-cycle")
    public BillingRunResponse processBillingCycle(
            @Parameter(description = "ISO-8601 instant to process as-of; defaults to now") @RequestParam(required = false) Instant asOf) {
        return mapper.toResponse(subscriptionService.processBillingCycle(asOf != null ? asOf : Instant.now()));
    }
}
