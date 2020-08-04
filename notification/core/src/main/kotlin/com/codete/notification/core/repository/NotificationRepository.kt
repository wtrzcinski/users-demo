package com.codete.notification.core.repository

import com.codete.notification.core.model.CreateNotification

interface NotificationRepository {
    fun create(message: CreateNotification)
}