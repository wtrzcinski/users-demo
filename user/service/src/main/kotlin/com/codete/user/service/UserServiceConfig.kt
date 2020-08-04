package com.codete.user.service

import com.codete.user.core.repository.Encryptor
import com.codete.user.core.repository.Notifications
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.encrypt.Encryptors

@Configuration
internal class UserServiceConfig {
    @Bean
    fun userService(notifications: Notifications, userRepository: UserRepository): UserService {
        return UserService(userRepository, notifications, encryptor())
    }

    private fun encryptor(): Encryptor {
        return object : Encryptor {
            private val queryableText = Encryptors.text("0123456789", "30313233343536373839")

            override fun encrypt(value: String?): String? {
                if (value == null) {
                    return null
                }
                return queryableText.encrypt(value)
            }
        }
    }
}