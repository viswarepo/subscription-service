package com.sms.sub.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A customer's subscription to one plan of one product.
 *
 * Pricing is snapshotted onto the subscription at creation/change time
 * (unitAmount, currency, billingCycle) rather than looked up live from the
 * catalog on every read. This is deliberate: a subscriber's price should not
 * silently change because the catalog was re-priced elsewhere — only an
 * explicit changePlan() call (immediate or scheduled) should move a
 * subscription onto new pricing. planCode/planVersion still identify exactly
 * which catalog plan version the subscriber is on, for entitlement checks.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant identifier, denormalized from the owning Customer so this table can be queried and indexed independently by tenant. */
    @Column(nullable = false)
    private String organizationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private String planCode;

    @Column(nullable = false)
    private int planVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitAmount;

    @Column(nullable = false)
    private Instant currentPeriodStart;

    @Column(nullable = false)
    private Instant currentPeriodEnd;

    private Instant trialEnd;

    @Column(nullable = false)
    private boolean cancelAtPeriodEnd = false;

    private Instant canceledAt;

    private Instant endedAt;

    // --- scheduled (deferred) plan change, applied on next renewal ---
    private String scheduledPlanCode;
    private Integer scheduledPlanVersion;
    private BigDecimal scheduledUnitAmount;
    @Enumerated(EnumType.STRING)
    private BillingCycle scheduledBillingCycle;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Subscription() {
        // JPA
    }

    public Subscription(String organizationId, Customer customer, String productCode, String planCode, int planVersion,
                         CurrencyCode currency, BillingCycle billingCycle, BigDecimal unitAmount) {
        this.organizationId = organizationId;
        this.customer = customer;
        this.productCode = productCode;
        this.planCode = planCode;
        this.planVersion = planVersion;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.unitAmount = unitAmount;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public boolean hasScheduledPlanChange() {
        return scheduledPlanCode != null;
    }

    public void clearScheduledPlanChange() {
        this.scheduledPlanCode = null;
        this.scheduledPlanVersion = null;
        this.scheduledUnitAmount = null;
        this.scheduledBillingCycle = null;
    }

    // ---- getters / setters ----

    public Long getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public int getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(int planVersion) {
        this.planVersion = planVersion;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }

    public Instant getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(Instant currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Instant currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public Instant getTrialEnd() {
        return trialEnd;
    }

    public void setTrialEnd(Instant trialEnd) {
        this.trialEnd = trialEnd;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Instant canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getScheduledPlanCode() {
        return scheduledPlanCode;
    }

    public Integer getScheduledPlanVersion() {
        return scheduledPlanVersion;
    }

    public BigDecimal getScheduledUnitAmount() {
        return scheduledUnitAmount;
    }

    public BillingCycle getScheduledBillingCycle() {
        return scheduledBillingCycle;
    }

    public void schedulePlanChange(String planCode, int planVersion, BigDecimal unitAmount, BillingCycle billingCycle) {
        this.scheduledPlanCode = planCode;
        this.scheduledPlanVersion = planVersion;
        this.scheduledUnitAmount = unitAmount;
        this.scheduledBillingCycle = billingCycle;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
