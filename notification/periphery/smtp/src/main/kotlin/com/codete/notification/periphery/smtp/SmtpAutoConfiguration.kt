package com.codete.notification.periphery.smtp

import com.codete.notification.core.repository.NotificationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
internal class SmtpAutoConfiguration {
    @Bean
    fun notificationRepository(emailSender: JavaMailSender): NotificationRepository {
        return SmtpNotificationRepository(emailSender)
    }
}