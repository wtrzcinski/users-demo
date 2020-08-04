package com.codete.notification.core.model

data class CreateNotification(
        val to: String,
        val subject: String,
        val text: String,
        val context: Map<String, String>
)
