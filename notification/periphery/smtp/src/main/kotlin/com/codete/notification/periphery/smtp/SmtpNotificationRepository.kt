package com.codete.notification.periphery.smtp

import com.codete.notification.core.model.CreateNotification
import com.codete.notification.core.repository.NotificationRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender


internal class SmtpNotificationRepository(private val emailSender: JavaMailSender) : NotificationRepository {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun create(message: CreateNotification) {
        log.info("create: $message")

        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.setFrom("noreply@notifications.com")
        simpleMailMessage.setTo(message.to)
        simpleMailMessage.setSubject(message.subject)
        simpleMailMessage.setText(message.text)

        emailSender.send(simpleMailMessage)
    }
}