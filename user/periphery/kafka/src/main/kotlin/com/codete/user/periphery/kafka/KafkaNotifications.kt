package com.codete.user.periphery.kafka

import com.codete.user.core.model.CreateNotificationRequest
import com.codete.user.core.repository.Notifications
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.kafka.core.KafkaTemplate
import reactor.core.publisher.Mono.fromFuture

internal class KafkaNotifications(
        private val kafkaTemplate: KafkaTemplate<String, String>,
        private val topic: KafkaTopicConfig,
        private val objectMapper: ObjectMapper
) : Notifications {
    override suspend fun accountCreated(notification: CreateNotificationRequest) {
        val message = objectMapper.writeValueAsString(mapOf(
                "to" to notification.user.email,
                "subject" to "account.created.subject",
                "text" to "account.created.text",
                "context" to notification.user
        ))
        fromFuture(kafkaTemplate.send(topic.topic!!, message).completable()).awaitFirstOrNull()
    }
}