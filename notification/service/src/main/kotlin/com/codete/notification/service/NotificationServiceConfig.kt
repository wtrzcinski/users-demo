package com.codete.notification.service

import com.codete.notification.core.repository.NotificationRepository
import com.codete.notification.core.repository.TemplateRepository
import com.codete.notification.core.service.NotificationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class NotificationServiceConfig {
    @Bean
    fun notificationService(
            notificationRepository: NotificationRepository,
            templateRepository: TemplateRepository
    ): NotificationService {
        return NotificationService(notificationRepository, templateRepository)
    }
}