package com.codete.user.periphery.kafka

import com.codete.user.core.repository.Notifications
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate


@Configuration
class KafkaAutoConfiguration {
    @Bean
    fun kafkaNotificationRepository(
            kafkaTemplate: KafkaTemplate<String, String>,
            topic: KafkaTopicConfig,
            objectMapper: ObjectMapper
    ): Notifications {
        return KafkaNotifications(
                kafkaTemplate,
                topic,
                objectMapper
        )
    }

    @Bean
    fun newTopic(topic: KafkaTopicConfig): NewTopic {
        return NewTopic(topic.topic, topic.numPartitions ?: 1, topic.replicationFactor ?: 1.toShort())
    }

    @Bean
    @ConfigurationProperties("spring.kafka.properties")
    fun topicConfig(): KafkaTopicConfig {
        return KafkaTopicConfig()
    }
}