package com.codete.user.core.service

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.core.repository.Encryptor
import com.codete.user.core.repository.Notifications
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.exception.NotFoundException
import com.codete.user.core.service.exception.ValidationException
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.ArgumentMatchers.anyString

class UserServicePatchTest {

    private val users = mock<UserRepository>()
    private val notifications = mock<Notifications>()
    private val encryptor = mock<Encryptor>()
    private val service = UserService(users, notifications, encryptor)

    @BeforeEach
    fun before() {
        encryptor.stub {
            onBlocking { encrypt(anyString()) } doAnswer returnsFirstArg<String>()
        }
    }

    @Test
    fun `should validate if id is provided`() {
        val patch = catchThrowable { runBlocking<Unit> { service.patch(PatchUser()) } }

        assertThat(patch)
                .isInstanceOf(ValidationException::class.java)
                .hasMessage("'id' field not provided");
    }

    @Test
    fun `should validate if properties to update are provided`() {
        val patch = catchThrowable { runBlocking<Unit> { service.patch(PatchUser(id = UserId("123"))) } }

        assertThat(patch)
                .isInstanceOf(ValidationException::class.java)
                .hasMessage("No field to update (updatable fields: 'password', 'firstName', 'lastName')")
    }

    @Test
    fun `should validate if user exists`() = runBlocking<Unit> {
        users.stub {
            onBlocking { exists(UserId("123")) } doReturn false
        }

        val patch = catchThrowable { runBlocking<Unit> { service.patch(PatchUser(id = UserId("123"), firstName = "first name", password = "password")) } }

        assertThat(patch)
                .isInstanceOf(NotFoundException::class.java)
                .hasMessage("User with id '123' does not exist")
    }

    @Test
    fun `should update`() = runBlocking<Unit> {
        users.stub {
            onBlocking { findById(UserId("123")) } doReturn User(UserId("123"), "username", email = "email")
            onBlocking { exists(UserId("123")) } doReturn true
        }

        val patch = service.patch(PatchUser(id = UserId("123"), firstName = "first name", password = "password"))

        assertThat(patch).isEqualTo(User(UserId("123"), "username", email = "email"))
        verify(users).update(PatchUser(UserId("123"), firstName = "first name", password = "password"))
        verify(encryptor).encrypt("password")
    }
}