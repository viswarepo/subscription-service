package com.sms.sub.producer;

import com.sms.sub.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriptionCancelledEventProducer {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public AlertSubscriptionCancelledEventProducer(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertSubscriptionCancelled(SubscriptionCreatedEvent event) {
        kafkaTemplate.send("alert.subscription.cancelled", event);
    }
}

