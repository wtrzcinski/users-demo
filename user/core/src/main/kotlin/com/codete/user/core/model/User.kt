package com.codete.user.core.model

data class User(
        val id: UserId,
        val username: String,
        val email: String,
        val firstName: String? = null,
        val lastName: String? = null
)