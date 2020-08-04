package com.codete.notification.periphery.template

import com.codete.notification.core.repository.TemplateRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemplateAutoConfiguration {
    @Bean
    fun templateRepository(): TemplateRepository {
        return StringTemplateRepository(templates())
    }

    @Bean
    @ConfigurationProperties("template")
    fun templates(): Map<String, String> {
        return mutableMapOf()
    }
}