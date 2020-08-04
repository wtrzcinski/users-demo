package com.codete.user.core.service

import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.DeleteUser
import com.codete.user.core.repository.Encryptor
import com.codete.user.core.repository.Notifications
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.exception.NotFoundException
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentMatchers

class UserServiceDeleteTest {
    private val users = mock<UserRepository>()
    private val notifications = mock<Notifications>()
    private val encryptor = mock<Encryptor>()
    private val service = UserService(users, notifications, encryptor)

    @BeforeEach
    fun before() {
        encryptor.stub {
            onBlocking { encrypt(ArgumentMatchers.anyString()) } doAnswer AdditionalAnswers.returnsFirstArg<String>()
        }
    }

    @Test
    fun `should validate if exists`() = runBlocking<Unit> {
        val userId = UserId("123")
        users.stub {
            onBlocking { exists(userId) } doReturn false
        }

        val patch = catchThrowable { runBlocking<Unit> { service.delete(DeleteUser(userId)) } }

        assertThat(patch)
                .isInstanceOf(NotFoundException::class.java)
                .hasMessage("User with id '123' does not exist")
    }

    @Test
    fun `should delete`() = runBlocking<Unit> {
        val userId = UserId("123")
        users.stub {
            onBlocking { exists(userId) } doReturn true
        }

        val patch = service.delete(DeleteUser(userId))

        assertThat(patch).isEqualTo(userId)
        verify(users).delete(userId)
    }
}