package com.sms.sub.producer;

import com.sms.sub.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriptionPayfailureEventProducer {

    private final KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate;

    public AlertSubscriptionPayfailureEventProducer(KafkaTemplate<String, SubscriptionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertSubscriptionRegistered(SubscriptionCreatedEvent event) {
        kafkaTemplate.send("alert.subscription.paymentfailure", event);
    }
}

