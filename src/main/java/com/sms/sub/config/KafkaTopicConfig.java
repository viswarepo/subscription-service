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
}
