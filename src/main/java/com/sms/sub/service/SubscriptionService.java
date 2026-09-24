package com.sms.sub.service;

import com.sms.sub.domain.*;
import com.sms.sub.dto.RegisterRequest;
import com.sms.sub.event.SubscriptionCreatedEvent;
import com.sms.sub.event.SubscriptionEventPublisher;
import com.sms.sub.exception.ConflictException;
import com.sms.sub.exception.InvalidSubscriptionStateException;
import com.sms.sub.exception.ResourceNotFoundException;
import com.sms.sub.repository.CustomerRepository;
import com.sms.sub.repository.SubscriptionEventRepository;
import com.sms.sub.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SubscriptionService {
    /* Kafka even publisher/Producer */
    private final SubscriptionEventPublisher subscriptionEventPublisher;

    /** Statuses that count as "the customer already has a live subscription to this product". */
    private static final Set<SubscriptionStatus> LIVE_STATUSES =
            Set.of(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE, SubscriptionStatus.PAUSED);

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventRepository eventRepository;

    @Value("${user-service.url}")
    private String userServiceBaseUrl;

    @Value("${internal.api-key}")
    private String internalApiKey;

    //private final RestTemplate restTemplate;

    public SubscriptionService(SubscriptionEventPublisher subscriptionEventPublisher, CustomerRepository customerRepository,
                               SubscriptionRepository subscriptionRepository,
                               SubscriptionEventRepository eventRepository) {
        this.subscriptionEventPublisher = subscriptionEventPublisher;
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
    }

    public Customer createCustomer(String organizationId, String email, String name, String mobile) {

        return customerRepository.save(new Customer(organizationId, email, name,mobile));
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(String organizationId, String customerId) {
        return customerRepository.findByIdAndOrganizationId(customerId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    @Transactional(readOnly = true)
    public List<Customer> listCustomers(String organizationId) {
        return customerRepository.findByOrganizationId(organizationId);
    }

    /**
     * Creates a new subscription. Starts TRIALING if trialDays > 0, otherwise ACTIVE
     * immediately. Refuses to create a second live (non-terminal) subscription for
     * the same customer + product — call changePlan on the existing one instead.
     */
    public Subscription createSubscription(String organizationId, String customerId, String productCode, String planCode,
                                            int planVersion, CurrencyCode currency, BillingCycle billingCycle,
                                            BigDecimal unitAmount, int trialDays) {
        Customer customer = getCustomer(organizationId, customerId);

        boolean alreadyLive = !subscriptionRepository
                .findByOrganizationIdAndCustomer_IdAndProductCodeAndStatusIn(organizationId, customerId, productCode, LIVE_STATUSES)
                .isEmpty();
        if (alreadyLive) {
            throw new ConflictException(
                    "Customer " + customerId + " already has a live subscription to product " + productCode);
        }

        Subscription sub = new Subscription(organizationId, customer, productCode, planCode, planVersion, currency, billingCycle, unitAmount);
        Instant now = Instant.now();
        sub.setCurrentPeriodStart(now);

        if (trialDays > 0) {
            Instant trialEnd = BillingCycleCalculator.addTrialDays(now, trialDays);
            sub.setStatus(SubscriptionStatus.TRIALING);
            sub.setTrialEnd(trialEnd);
            sub.setCurrentPeriodEnd(trialEnd);
        } else {
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setCurrentPeriodEnd(BillingCycleCalculator.addOneCycle(now, billingCycle));
        }

        subscriptionRepository.save(sub);
        recordEvent(sub, SubscriptionEventType.CREATED, "Subscription created in status " + sub.getStatus());

        SubscriptionCreatedEvent event = SubscriptionCreatedEvent.of(
                sub.getId(),
                organizationId,
                customer.getId(),           // however you reference the Customer here
                customer.getEmail(),
                customer.getName(),
                productCode,                 // whatever the actual local variable/param name is
                planCode,
                planVersion,
                unitAmount,
                currency.name(),              // .toString() if currency isn't an enum
                billingCycle.name(),          // .toString() if billingCycle isn't an enum
                trialDays);
        subscriptionEventPublisher.publishSubscriptionCreated(event);


        return sub;
    }

    @Transactional(readOnly = true)
    public Subscription getSubscription(String organizationId, String subscriptionId) {
        return subscriptionRepository.findByIdAndOrganizationId(subscriptionId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscription(String organizationId) {
        return subscriptionRepository.findByOrganizationId(organizationId);

    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsForCustomer(String organizationId, String customerId) {
        return subscriptionRepository.findByOrganizationIdAndCustomer_Id(organizationId, customerId);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionEvent> getEvents(String organizationId, String subscriptionId) {
        return eventRepository.findBySubscription_IdAndOrganizationIdOrderByOccurredAtDesc(subscriptionId, organizationId);
    }

    // ---------------------------------------------------------------- Trial / payment outcomes

    /** Converts a trial to a paid subscription immediately (e.g. customer added a card and confirmed early). */
    public Subscription convertTrial(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        validateTransition(sub.getStatus(), SubscriptionStatus.ACTIVE);

        Instant now = Instant.now();
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(BillingCycleCalculator.addOneCycle(now, sub.getBillingCycle()));
        sub.touch();
        recordEvent(sub, SubscriptionEventType.TRIAL_CONVERTED, "Trial converted to paid subscription");
        return sub;
    }

    /** Records a successful charge (trial conversion or renewal retry) and (re)activates the subscription. */
    public Subscription recordPaymentSuccess(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        if (sub.getStatus() == SubscriptionStatus.TRIALING) {
            return convertTrial(organizationId, subscriptionId);
        }
        validateTransition(sub.getStatus(), SubscriptionStatus.ACTIVE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.touch();
        recordEvent(sub, SubscriptionEventType.PAYMENT_SUCCEEDED, "Payment succeeded, subscription active");
        return sub;
    }

    /** Records a failed renewal/conversion charge and moves the subscription into dunning. */
    public Subscription recordPaymentFailure(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        validateTransition(sub.getStatus(), SubscriptionStatus.PAST_DUE);
        sub.setStatus(SubscriptionStatus.PAST_DUE);
        sub.touch();
        recordEvent(sub, SubscriptionEventType.PAYMENT_FAILED, "Payment failed, subscription past due");
        return sub;
    }

    // ---------------------------------------------------------------- Renewal

    /**
     * Advances a subscription to its next billing period. If a cancellation was
     * scheduled for period end, finalizes the cancellation instead of renewing.
     * If a plan change was scheduled, applies it as part of the renewal.
     */
    public Subscription renew(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InvalidSubscriptionStateException(
                    "Only ACTIVE subscriptions can renew (current status: " + sub.getStatus() + ")");
        }

        if (sub.isCancelAtPeriodEnd()) {
            return finalizeCancellation(sub);
        }

        if (sub.hasScheduledPlanChange()) {
            sub.setPlanCode(sub.getScheduledPlanCode());
            sub.setPlanVersion(sub.getScheduledPlanVersion());
            sub.setUnitAmount(sub.getScheduledUnitAmount());
            sub.setBillingCycle(sub.getScheduledBillingCycle());
            sub.clearScheduledPlanChange();
            recordEvent(sub, SubscriptionEventType.PLAN_CHANGED, "Scheduled plan change applied at renewal");
        }

        Instant newPeriodStart = sub.getCurrentPeriodEnd();
        sub.setCurrentPeriodStart(newPeriodStart);
        sub.setCurrentPeriodEnd(BillingCycleCalculator.addOneCycle(newPeriodStart, sub.getBillingCycle()));
        sub.touch();
        recordEvent(sub, SubscriptionEventType.RENEWED,
                "Renewed through " + sub.getCurrentPeriodEnd());
        return sub;
    }

    private Subscription finalizeCancellation(Subscription sub) {
        sub.setStatus(SubscriptionStatus.CANCELED);
        sub.setEndedAt(sub.getCurrentPeriodEnd());
        sub.touch();
        recordEvent(sub, SubscriptionEventType.CANCELED, "Scheduled cancellation finalized at period end");
        return sub;
    }

    // ---------------------------------------------------------------- Plan changes

    /**
     * Changes the plan a subscription is on. If immediate, takes effect now
     * (current period is kept as-is; a production system would prorate the
     * difference here — see README). If not immediate, the change is staged
     * and applied automatically on the next renewal.
     */
    public Subscription changePlan(String organizationId, String subscriptionId, String newPlanCode, int newPlanVersion,
                                    BigDecimal newUnitAmount, BillingCycle newBillingCycle, boolean immediate) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE
                && sub.getStatus() != SubscriptionStatus.TRIALING
                && sub.getStatus() != SubscriptionStatus.PAST_DUE) {
            throw new InvalidSubscriptionStateException(
                    "Cannot change plan while subscription is " + sub.getStatus());
        }

        if (immediate) {
            sub.setPlanCode(newPlanCode);
            sub.setPlanVersion(newPlanVersion);
            sub.setUnitAmount(newUnitAmount);
            sub.setBillingCycle(newBillingCycle);
            sub.touch();
            recordEvent(sub, SubscriptionEventType.PLAN_CHANGED,
                    "Immediate plan change to " + newPlanCode + " v" + newPlanVersion
                            + " (no proration applied in this sample)");
        } else {
            sub.schedulePlanChange(newPlanCode, newPlanVersion, newUnitAmount, newBillingCycle);
            sub.touch();
            recordEvent(sub, SubscriptionEventType.PLAN_CHANGE_SCHEDULED,
                    "Plan change to " + newPlanCode + " v" + newPlanVersion + " scheduled for next renewal");
        }
        return sub;
    }

    // ---------------------------------------------------------------- Cancellation

    /**
     * Cancels a subscription. atPeriodEnd=true schedules the cancellation to
     * take effect at the current period's end (status unchanged until then);
     * atPeriodEnd=false cancels immediately.
     */
    public Subscription cancel(String organizationId, String subscriptionId, boolean atPeriodEnd) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        if (sub.getStatus() == SubscriptionStatus.CANCELED || sub.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new InvalidSubscriptionStateException("Subscription is already " + sub.getStatus());
        }

        Instant now = Instant.now();
        if (atPeriodEnd) {
            sub.setCancelAtPeriodEnd(true);
            sub.setCanceledAt(now);
            sub.touch();
            recordEvent(sub, SubscriptionEventType.CANCELLATION_SCHEDULED,
                    "Cancellation scheduled for " + sub.getCurrentPeriodEnd());
        } else {
            validateTransition(sub.getStatus(), SubscriptionStatus.CANCELED);
            sub.setStatus(SubscriptionStatus.CANCELED);
            sub.setCancelAtPeriodEnd(false);
            sub.setCanceledAt(now);
            sub.setEndedAt(now);
            sub.touch();
            recordEvent(sub, SubscriptionEventType.CANCELED, "Canceled immediately");
        }
        return sub;
    }

    /** Undoes a scheduled (not-yet-effective) cancellation. An already-CANCELED subscription cannot be reactivated — create a new one instead. */
    public Subscription reactivate(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        if (!sub.isCancelAtPeriodEnd()) {
            throw new InvalidSubscriptionStateException(
                    "Subscription " + subscriptionId + " has no scheduled cancellation to undo");
        }
        sub.setCancelAtPeriodEnd(false);
        sub.setCanceledAt(null);
        sub.touch();
        recordEvent(sub, SubscriptionEventType.REACTIVATED, "Scheduled cancellation undone");
        return sub;
    }

    // ---------------------------------------------------------------- Pause / resume

    public Subscription pause(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        validateTransition(sub.getStatus(), SubscriptionStatus.PAUSED);
        sub.setStatus(SubscriptionStatus.PAUSED);
        sub.touch();
        recordEvent(sub, SubscriptionEventType.PAUSED, "Subscription paused");
        return sub;
    }

    public Subscription resume(String organizationId, String subscriptionId) {
        Subscription sub = getSubscription(organizationId, subscriptionId);
        validateTransition(sub.getStatus(), SubscriptionStatus.ACTIVE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.touch();
        recordEvent(sub, SubscriptionEventType.RESUMED, "Subscription resumed");
        return sub;
    }

    // ---------------------------------------------------------------- Batch billing cycle processing

    /**
     * Simulates a billing run: finalizes/renews every subscription whose current
     * period has ended as of `asOf`, and expires trials that ran out without
     * converting. In this sample no real payment gateway is called — ACTIVE
     * subscriptions are assumed to renew successfully; a production system would
     * call a payment provider here and route failures to recordPaymentFailure
     * instead of renewing directly.
     *
     * Deliberately cross-tenant: this is an internal operational job, not a
     * tenant-facing read, so it is not organizationId-scoped. Every row it
     * touches (and every event it records) still carries its own
     * organizationId, so per-tenant reporting on the outcome remains possible.
     */
    public BillingRunResult processBillingCycle(Instant asOf) {
        int renewed = 0, canceled = 0, expired = 0, trialsExpired = 0;

        List<Subscription> due = subscriptionRepository.findByStatusInAndCurrentPeriodEndLessThanEqual(
                Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE), asOf);

        for (Subscription sub : due) {
            switch (sub.getStatus()) {
                case ACTIVE -> {
                    if (sub.isCancelAtPeriodEnd()) {
                        finalizeCancellation(sub);
                        canceled++;
                    } else {
                        renew(sub.getOrganizationId(), sub.getId());
                        renewed++;
                    }
                }
                case PAST_DUE -> {
                    // Dunning exhausted: still unpaid by period end -> involuntary churn.
                    sub.setStatus(SubscriptionStatus.EXPIRED);
                    sub.setEndedAt(asOf);
                    sub.touch();
                    recordEvent(sub, SubscriptionEventType.EXPIRED, "Dunning exhausted at period end");
                    expired++;
                }
                default -> {
                    // unreachable given the query filter, kept for exhaustiveness/clarity
                }
            }
        }

        List<Subscription> staleTrials = subscriptionRepository.findByStatusAndTrialEndLessThanEqual(
                SubscriptionStatus.TRIALING, asOf);
        for (Subscription sub : staleTrials) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setEndedAt(asOf);
            sub.touch();
            recordEvent(sub, SubscriptionEventType.EXPIRED, "Trial ended without conversion");
            trialsExpired++;
        }

        return new BillingRunResult(renewed, canceled, expired, trialsExpired);
    }

    /** Summary of a processBillingCycle() run, useful for logging/monitoring. */
    public record BillingRunResult(int renewed, int canceledAtPeriodEnd, int expiredFromDunning, int trialsExpired) {
    }

    // ---------------------------------------------------------------- Internal helpers

    private void recordEvent(Subscription sub, SubscriptionEventType type, String notes) {
        eventRepository.save(new SubscriptionEvent(sub.getOrganizationId(), sub, type, notes));
    }

    /**
     * Enforces the allowed lifecycle transitions. Terminal states (CANCELED,
     * EXPIRED) never transition out; a new subscription must be created instead.
     */
    private void validateTransition(SubscriptionStatus current, SubscriptionStatus target) {
        boolean valid = switch (current) {
            case TRIALING -> target == SubscriptionStatus.ACTIVE
                    || target == SubscriptionStatus.CANCELED
                    || target == SubscriptionStatus.EXPIRED
                    || target == SubscriptionStatus.PAST_DUE;
            case ACTIVE -> target == SubscriptionStatus.PAST_DUE
                    || target == SubscriptionStatus.CANCELED
                    || target == SubscriptionStatus.PAUSED;
            case PAST_DUE -> target == SubscriptionStatus.ACTIVE
                    || target == SubscriptionStatus.CANCELED
                    || target == SubscriptionStatus.EXPIRED;
            case PAUSED -> target == SubscriptionStatus.ACTIVE
                    || target == SubscriptionStatus.CANCELED;
            case CANCELED, EXPIRED -> false;
        };
        if (!valid) {
            throw new InvalidSubscriptionStateException(
                    "Illegal subscription status transition: " + current + " -> " + target);
        }
    }
}
