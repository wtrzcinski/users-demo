package com.codete.user.periphery.web.controller

data class ResultWithStatus<T>(
        val status: Int,
        val content: T?
)