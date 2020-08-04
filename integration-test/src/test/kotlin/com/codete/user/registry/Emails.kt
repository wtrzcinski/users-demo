package com.codete.user.registry

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient

/**
 * https://hub.docker.com/r/reachfive/fake-smtp-server
 */
object Emails {
    private val webClient = WebClient.builder()
            .baseUrl(System.getenv("service.emails.url") ?: "http://localhost:1080")
            .defaultHeader("Content-Type", "application/json")
            .build()

    data class Email(
            val subject: String,
            val text: String
    )

    fun deleteAll() {
        webClient.delete()
                .uri("api/emails")
                .exchange()
                .blockOptional()
    }

    fun findAll(): List<Email>? {
        val block = webClient.get()
                .uri("api/emails")
                .exchange()
                .flatMap { it.bodyToMono(parametrizedType<List<Map<*, *>>>()) }
                .flatMapIterable { it }
                .map { Email(subject = it["subject"] as String, text = it["text"] as String) }
                .collectList()
                .block()
        return block
    }

    fun findByEmail(email: String): List<Email>? {
        Thread.sleep(1000) // todo
        val block = webClient.get()
                .uri("api/emails?to={to}", mapOf("to" to email))
                .exchange()
                .flatMap { it.bodyToMono(parametrizedType<List<Map<*, *>>>()) }
                .flatMapIterable { it }
                .map { Email(subject = it["subject"] as String, text = it["text"] as String) }
                .collectList()
                .block()
        return block
    }

    private inline fun <reified T> parametrizedType() = object : ParameterizedTypeReference<T>() {}
}