package com.codete.user.core.repository

import com.codete.user.core.model.CreateNotificationRequest

interface Notifications {
    suspend fun accountCreated(notification: CreateNotificationRequest)
}
