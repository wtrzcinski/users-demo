package com.codete.user.registry

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import java.lang.System.getenv

object Users {
    private val webClient = WebClient.builder()
            .baseUrl(getenv("service.users.url") ?: "http://localhost:8080")
            .defaultHeader("Content-Type", "application/json")
            .build()

    data class User(
            val id: String? = null,
            val username: String,
            val email: String,
            val firstName: String?,
            val lastName: String?
    )

    fun delete(id: String) {
        webClient.delete()
                .uri("/user/{id}", mapOf("id" to id))
                .retrieve()
                .bodyToMono(Void::class.java)
                .block()
    }

    fun deleteAll() {
        webClient.delete()
                .uri("/user")
                .exchange()
                .blockOptional()
    }

    fun findAll(): List<Map<*, *>> {
        val block = webClient.get()
                .uri("/user")
                .retrieve()
                .bodyToMono(parametrizedType<List<Map<*, *>>>())
                .block()!!
        return block
    }

    fun findByUsername(username: String): User? {
        val blockFirst = webClient.get()
                .uri("/user?username={username}", mapOf("username" to username))
                .exchange()
                .flatMap { it.bodyToMono(parametrizedType<List<Map<*, *>>>()) }
                .flatMapIterable { it }
                .filter { it["username"] as String == username }
                .map { toUser(it) }
                .blockFirst()
        return blockFirst
    }

    fun updateByUsername(username: String, firstName: String) {
        val user = findByUsername(username)
        webClient.patch()
                .uri("/user/{id}", mapOf("id" to user?.id))
                .bodyValue(mapOf(
                        "firstName" to firstName
                ))
                .exchange()
                .flatMap { it.bodyToMono(parametrizedType<Map<*, *>>()) }
                .map { it }
                .blockOptional()
    }

    fun create(username: String, email: String, password: String): String {
        val block: String? = webClient.post()
                .uri("/user")
                .bodyValue(mapOf(
                        "username" to username,
                        "password" to password,
                        "email" to email
                ))
                .exchange()
                .flatMap { it.bodyToMono(parametrizedType<Map<*, *>>()) }
                .map { it["id"] as String? }
                .block()
        return block!!
    }

    fun deleteByUsername(username: String) {
        val user = findByUsername(username)
        webClient.delete()
                .uri("/user/{id}", mapOf("id" to user?.id))
                .exchange()
                .blockOptional()
    }

    fun updateBulk(content: List<Map<*, *>>) {
        webClient.post()
                .uri("/user/bulk")
                .bodyValue(content)
                .exchange()
                .blockOptional()
    }

    private fun toUser(it: Map<*, *>) = User(id = it["id"] as String, username = it["username"] as String, email = it["email"] as String, firstName = it["firstName"] as String?, lastName = it["lastName"] as String?)

    private inline fun <reified T> parametrizedType() = object : ParameterizedTypeReference<T>() {}
}