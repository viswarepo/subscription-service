package com.sms.sub.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SUBSCRIPTION_CREATED_TOPIC = "subscription.created";

    @Bean
    public NewTopic subscriptionCreatedTopic() {
        return TopicBuilder.name(SUBSCRIPTION_CREATED_TOPIC)
                .partitions(3)
                .replicas(1) // single-broker local dev default - raise this for a real cluster
                .build();
    }

    @Bean
    public NewTopic customerRegisteredTopic() {
        return TopicBuilder.name("alert.customer.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertSubscriptionCreatedTopic() {
        return TopicBuilder.name("alert.subscription.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sendAlertCancelledTopic() {
        return TopicBuilder.name("alert.subscription.cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertResumeTopic() {
        return TopicBuilder.name("alert.subscription.resume")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertPauseTopic() {
        return TopicBuilder.name("alert.subscription.paused")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertReactivateTopic() {
        return TopicBuilder.name("alert.subscription.reactivate")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertRenevTopic() {
        return TopicBuilder.name("alert.subscription.renew")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertPaymentSuccessTopic() {
        return TopicBuilder.name("alert.subscription.paymentsuccess")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertPaymentFailTopic() {
        return TopicBuilder.name("alert.subscription.paymentfail")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sendAlertChangePlanTopic() {
        return TopicBuilder.name("alert.change.plan")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
