package com.sms.sub.producer;

import com.sms.sub.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriptionCreatedEventProducer {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public AlertSubscriptionCreatedEventProducer(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertSubscriptionCreated(SubscriptionCreatedEvent event) {
        kafkaTemplate.send("alert.subscription.created", event);
    }
}

