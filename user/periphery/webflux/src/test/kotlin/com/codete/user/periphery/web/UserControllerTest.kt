package com.codete.user.periphery.web

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.DeleteUser
import com.codete.user.core.model.operation.FindUser
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.core.service.UserService
import com.codete.user.periphery.web.config.ObjectMapperConfig
import com.codete.user.periphery.web.controller.UserController
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus.MULTI_STATUS
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux.just

@FlowPreview
@WebFluxTest(UserController::class)
class UserControllerTest {

    @Configuration
    @Import(ObjectMapperConfig::class)
    @SpringBootApplication
    internal class Config

    @MockBean
    private lateinit var service: UserService

    @Test
    fun `should create user`(@Autowired service: UserService, @Autowired client: WebTestClient) = runBlocking<Unit> {
        service.stub {
            onBlocking { create(CreateUser("username", "email@email", "password")) } doReturn User(UserId("123"), "username", "email@email")
        }

        client.post()
                .uri("/user")
                .bodyValue(mapOf(
                        "username" to "username",
                        "email" to "email@email",
                        "password" to "password"
                ))
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody<Map<*, *>>()
                .isEqualTo(mapOf(
                        "id" to "123",
                        "username" to "username",
                        "email" to "email@email"
                ))
    }


    @Test
    fun `should return users`(@Autowired service: UserService, @Autowired client: WebTestClient) = runBlocking<Unit> {
        service.stub {
            onBlocking { findAll(FindUser()) } doReturn just(User(UserId("124"), "username", "email@email")).asFlow()
        }

        client.get()
                .uri("/user")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<*>>()
                .isEqualTo(listOf(
                        mapOf(
                                "id" to "124",
                                "username" to "username",
                                "email" to "email@email"
                        )
                ))
    }

    @Test
    fun `should update users`(@Autowired service: UserService, @Autowired client: WebTestClient) = runBlocking<Unit> {
        service.stub {
            onBlocking { patch(PatchUser(id = UserId("123"), password = "password", firstName = "firstName", lastName = "lastName")) } doReturn User(id = UserId("123"), username = "username", email = "email@email")
        }

        client.patch()
                .uri("/user/{id}", mapOf("id" to "123"))
                .bodyValue(mapOf(
                        "password" to "password",
                        "firstName" to "firstName",
                        "lastName" to "lastName"
                ))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody<Map<*, *>>()
                .isEqualTo(mapOf(
                        "id" to "123",
                        "username" to "username",
                        "email" to "email@email"
                ))
    }

    @Test
    fun `should update multiple users`(@Autowired service: UserService, @Autowired client: WebTestClient) = runBlocking<Unit> {
        service.stub {
            onBlocking { create(CreateUser("username", "email123123", "password")) } doReturn User(UserId("123"), "username", "email@email")
            onBlocking { patch(PatchUser(id = UserId("124"), password = "password")) } doReturn User(id = UserId("124"), username = "username", email = "email@email")
            onBlocking { delete(DeleteUser(id = UserId("125"))) } doReturn UserId("125")

        }

        client.post()
                .uri("/user/bulk")
                .bodyValue(listOf(
                        mapOf(
                                "type" to "create",
                                "username" to "username",
                                "password" to "password",
                                "email" to "email123123"
                        ),
                        mapOf(
                                "type" to "patch",
                                "password" to "password",
                                "id" to "124"
                        ),
                        mapOf(
                                "type" to "delete",
                                "id" to "125"
                        )
                ))
                .exchange()
                .expectStatus()
                .isEqualTo(MULTI_STATUS)
                .expectBody<List<*>>()
                .isEqualTo(listOf(
                        mapOf(
                                "status" to 201,
                                "content" to mapOf(
                                        "id" to "123",
                                        "username" to "username",
                                        "email" to "email@email"
                                )
                        ),
                        mapOf(
                                "status" to 200,
                                "content" to mapOf(
                                        "id" to "124",
                                        "username" to "username",
                                        "email" to "email@email"
                                )
                        ),
                        mapOf(
                                "status" to 204,
                                "content" to mapOf(
                                        "id" to "125"
                                )
                        )
                ))
    }
}