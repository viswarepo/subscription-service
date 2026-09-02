package com.sms.sub.repository;

import com.sms.sub.domain.SubscriptionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, Long> {
    List<SubscriptionEvent> findBySubscription_IdAndOrganizationIdOrderByOccurredAtDesc(
            Long subscriptionId, String organizationId);
}
