package com.codete.notification.core.repository

interface TemplateRepository {
    fun create(name: String, context: Map<String, String>): String
}
