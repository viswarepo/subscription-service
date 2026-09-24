package com.sms.sub.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal customer record. In a real system this would likely be owned by a
 * separate Customer/Identity service and referenced here by ID only; it's
 * kept local in this sample so the service is self-contained and runnable.
 *
 * organizationId identifies the tenant (the business using this platform,
 * not the end customer) that owns this record. Every query in this service
 * is scoped by it so one tenant can never read or modify another's data.
 */
@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organizationId", "externalId"})
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Tenant identifier. Every row in every table in this service carries one. */
    @Column(nullable = false)
    private String organizationId;

    /** Business key from the system of record (CRM, auth provider, etc.), unique within the organization. */
    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String email="9876543210";

    @Column(nullable = false)
    private String mobile;

    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Customer() {
        // JPA
    }

    public Customer(String organizationId, String email, String name, String mobile) {
        this.organizationId = organizationId;
        this.externalId = externalId;
        this.email = email;
        this.name = name;
        this.mobile = mobile;
    }

    public String getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() { return mobile; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (externalId == null) {
            externalId = UUID.randomUUID().toString(); // ✅ auto-generate
        }
    }
}
