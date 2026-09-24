package com.sms.sub.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Append-only audit trail entry for a subscription lifecycle change.
 * Kept even after the subscription reaches a terminal state, so support and
 * analytics can reconstruct history independent of current row state.
 */
@Entity
@Table(name = "subscription_events")
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Tenant identifier, denormalized from the owning Subscription so audit queries stay tenant-scoped. */
    @Column(nullable = false)
    private String organizationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionEventType type;

    @Column(nullable = false)
    private Instant occurredAt = Instant.now();

    private String notes;

    protected SubscriptionEvent() {
        // JPA
    }

    public SubscriptionEvent(String organizationId, Subscription subscription, SubscriptionEventType type, String notes) {
        this.organizationId = organizationId;
        this.subscription = subscription;
        this.type = type;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public SubscriptionEventType getType() {
        return type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getNotes() {
        return notes;
    }
}
