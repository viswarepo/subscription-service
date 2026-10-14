package com.sms.sub.producer;

import com.sms.sub.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriptionChangeplanEventProducer {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public AlertSubscriptionChangeplanEventProducer(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertSubscriptionChangePlan(SubscriptionCreatedEvent event) {
        kafkaTemplate.send("alert.changeplan", event);
    }
}

