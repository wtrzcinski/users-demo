package com.codete.notification.periphery.kafka

import com.codete.notification.core.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// todo add retry policy
@Configuration
class KafkaAutoConfiguration() {
    @Bean
    fun kafkaConsumer(mapper: ObjectMapper, notificationService: NotificationService): KafkaConsumer {
        return KafkaConsumer(mapper, notificationService)
    }

    @Bean
    fun newTopic(config: KafkaTopicConfig): NewTopic {
        return NewTopic(config.topic, config.numPartitions ?: 1, config.replicationFactor ?: 1.toShort())
    }

    @Bean
    @ConfigurationProperties("spring.kafka.properties")
    fun topicConfig(): KafkaTopicConfig {
        return KafkaTopicConfig()
    }
}