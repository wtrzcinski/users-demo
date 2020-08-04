package com.codete.notification.periphery.kafka

import com.codete.notification.core.model.CreateNotification
import com.codete.notification.core.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener

class KafkaConsumer(
        private val mapper: ObjectMapper,
        private val notificationService: NotificationService
) {
    @KafkaListener(topics = ["\${spring.kafka.properties.topic}"], groupId = "notification-service")
    fun listen(message: String) {
        val to = mapper.readValue(message, CreateNotification::class.java)
        notificationService.create(to)
    }
}