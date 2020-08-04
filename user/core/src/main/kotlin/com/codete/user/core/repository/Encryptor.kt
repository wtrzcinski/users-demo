package com.codete.user.core.repository

interface Encryptor {
    fun encrypt(value: String?): String?
}