package com.sms.sub.event;

import com.sms.sub.config.KafkaTopicConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes subscription lifecycle events for other services (billing-payment-service)
 * to consume asynchronously.
 *
 * KNOWN LIMITATION - the dual-write problem: this publish happens as a
 * separate step after the database commit in SubscriptionService, not
 * atomically with it. If the app crashes between the DB commit and this
 * call succeeding, the master Subscription record exists but the event
 * never got published, and billing-payment-service will never learn about
 * it. For a real production system handling money, the standard fix is a
 * transactional outbox (write the event to an outbox table in the SAME
 * transaction as the Subscription insert, then a separate poller/CDC
 * process publishes from that table to Kafka reliably). Not implemented
 * here - this is the simpler direct-publish version; revisit if that gap
 * matters for your reliability requirements.
 */
@Component
@Slf4j
public class SubscriptionEventPublisher {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public SubscriptionEventPublisher(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSubscriptionCreated(SubscriptionCreatedEvent event) {
        // Keyed by organizationId so all events for one tenant land on the
        // same partition, preserving per-tenant ordering if that ever matters.
        kafkaTemplate.send(KafkaTopicConfig.SUBSCRIPTION_CREATED_TOPIC, event.organizationId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish SubscriptionCreatedEvent for subscription {}: {}",
                                event.subscriptionId(),
                                ex.getMessage(),
                                ex);
                    } else {
                        log.info("Published SubscriptionCreatedEvent {} for subscription {} (partition={}, offset={})",
                                event.eventId(),
                                event.subscriptionId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
