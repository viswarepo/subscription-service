package com.sms.sub.producer;

import com.sms.sub.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriptionResumeEventProducer {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public AlertSubscriptionResumeEventProducer(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertSubscriptionRenew(SubscriptionCreatedEvent event) {
        kafkaTemplate.send("alert.subscription.resume", event);
    }
}

