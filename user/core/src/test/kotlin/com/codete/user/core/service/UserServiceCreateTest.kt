package com.codete.user.core.service

import com.codete.user.core.model.CreateNotificationRequest
import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.repository.Encryptor
import com.codete.user.core.repository.Notifications
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.exception.DuplicateException
import com.codete.user.core.service.exception.ServiceException
import com.codete.user.core.service.exception.ValidationException
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.ArgumentMatchers

class UserServiceCreateTest {
    private val users = mock<UserRepository>()
    private val notifications = mock<Notifications>()
    private val encryptor = mock<Encryptor>()
    private val service = UserService(users, notifications, encryptor)

    @BeforeEach
    fun before() {
        encryptor.stub {
            onBlocking { encrypt(ArgumentMatchers.anyString()) } doAnswer returnsFirstArg<String>()
        }
    }

    @Test
    fun `should validate if password is provided`() {
        val create = catchThrowable { runBlocking<Unit> { service.create(CreateUser(username = "username", email = "email")) } } as ServiceException

        assertThat(create)
                .isInstanceOf(ValidationException::class.java)
                .hasMessage("'password' field is required")
    }

    @Test
    fun `should validate if email is correct`() {
        val create = catchThrowable { runBlocking<Unit> { service.create(CreateUser(username = "username", password = "password", email = "email")) } } as ServiceException

        assertThat(create)
                .isInstanceOf(ValidationException::class.java)
                .hasMessage("Email is not valid 'email'")
    }

    @Test
    fun `should validate if username already exists`() {
        users.stub {
            onBlocking { existsByUsername(any()) } doReturn true
        }

        val create = catchThrowable { runBlocking<Unit> { service.create(CreateUser(username = "username", email = "email@email.com", password = "password")) } }

        assertThat(create)
                .isInstanceOf(DuplicateException::class.java)
                .hasMessage("User with name 'username' already exists")
    }

    @Test
    fun `should create`() = runBlocking<Unit> {
        val request = CreateUser(username = "username", email = "email@email.com", password = "password")
        val user = User(UserId("123"), "username", email = "email@email.com")
        users.stub {
            onBlocking { create(request) } doReturn user
            onBlocking { existsByUsername(any()) } doReturn false
        }

        val create = service.create(request)

        assertThat(create).isEqualTo(user)
        verify(users).create(request)
        verify(notifications).accountCreated(CreateNotificationRequest(User(UserId("123"), "username", "email@email.com")))
        verify(encryptor).encrypt("password")
    }
}