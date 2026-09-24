package com.sms.sub.repository;

import com.sms.sub.domain.Subscription;
import com.sms.sub.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findByIdAndOrganizationId(String id, String organizationId);

    List<Subscription> findByOrganizationIdAndCustomer_Id(String organizationId, String customerId);

    List<Subscription> findByOrganizationId(String organizationId);

    List<Subscription> findByOrganizationIdAndCustomer_IdAndProductCodeAndStatusIn(
            String organizationId, String customerId, String productCode, Collection<SubscriptionStatus> statuses);

    /**
     * Intentionally NOT organization-scoped: the billing run is a cross-tenant
     * operational job (see BillingCycleScheduler / the process-billing-cycle
     * endpoint), not a tenant-facing read. Each affected row still carries its
     * own organizationId, so per-tenant reporting on the result remains possible.
     */
    List<Subscription> findByStatusInAndCurrentPeriodEndLessThanEqual(
            Collection<SubscriptionStatus> statuses, Instant asOf);

    List<Subscription> findByStatusAndTrialEndLessThanEqual(SubscriptionStatus status, Instant asOf);
}
