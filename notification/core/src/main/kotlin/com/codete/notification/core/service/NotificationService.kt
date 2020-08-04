package com.codete.notification.core.service

import com.codete.notification.core.model.CreateNotification
import com.codete.notification.core.repository.NotificationRepository
import com.codete.notification.core.repository.TemplateRepository

class NotificationService(
        private val notificationRepository: NotificationRepository,
        private val templateRepository: TemplateRepository
) {
    fun create(notification: CreateNotification) {
        val subject = templateRepository.create(notification.subject, notification.context)
        val text = templateRepository.create(notification.text, notification.context)
        notificationRepository.create(notification.copy(subject = subject, text = text))
    }
}