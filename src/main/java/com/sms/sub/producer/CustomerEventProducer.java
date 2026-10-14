package com.sms.sub.producer;


import com.sms.sub.event.CustomerRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerEventProducer {

    private final KafkaTemplate<String, CustomerRegisteredEvent> kafkaTemplate;

    public CustomerEventProducer(KafkaTemplate<String, CustomerRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        kafkaTemplate.send("alert.customer.registered", event);
    }
}
