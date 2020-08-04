package com.codete.user.periphery.r2dbc

import com.codete.user.core.model.User
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.FindUser
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.periphery.r2dbc.config.R2dbcAutoConfiguration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.DatabaseClient

@FlowPreview
@DataR2dbcTest
internal class DelegatingR2dbcUserRepositoryTest {
    @Configuration
    @Import(R2dbcAutoConfiguration::class)
    internal class R2DBCConfiguration {
        @Autowired
        fun init(databas: DatabaseClient) {
            databas.execute("""
                CREATE TABLE USERS (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(255),
                    first_name VARCHAR(255),
                    last_name VARCHAR(255),
                    email VARCHAR(255),
                    password VARCHAR(255)
                );
            """.trimIndent())
                    .then()
                    .subscribe()
        }
    }

    @BeforeEach
    fun beforeEach(@Autowired databas: DatabaseClient) {
        databas.delete().from(R2dbcUser::class.java).then().subscribe()
    }

    @Test
    fun `should create new user`(@Autowired repository: DelegatingR2dbcUserRepository) = runBlocking<Unit> {
        val id = repository.create(CreateUser(username = "username", email = "email", firstName = "firstName", lastName = "lastName", password = "password")).id

        val findById = repository.findById(id)
        val exists = repository.exists(id)

        assertThat(findById).isEqualTo(User(id = id, email = "email", username = "username", firstName = "firstName", lastName = "lastName"))
        assertThat(exists).isTrue()
    }

    @Test
    fun `should update existing user`(@Autowired repository: DelegatingR2dbcUserRepository) = runBlocking<Unit> {
        val id = repository.create(CreateUser(username = "username", email = "email", firstName = "firstName", lastName = "lastName")).id

        repository.update(PatchUser(id, firstName = "xxxxxx"))
        val findById = repository.findById(id)

        assertThat(findById).isEqualTo(User(id = id, email = "email", username = "username", firstName = "xxxxxx", lastName = "lastName"))
    }

    @Test
    fun `should delete existing user`(@Autowired repository: DelegatingR2dbcUserRepository) = runBlocking<Unit> {
        val id = repository.create(CreateUser(username = "username", email = "email", firstName = "firstName", lastName = "lastName")).id

        repository.delete(id)
        val findById = repository.findById(id)
        val exists = repository.exists(id)

        assertThat(findById).isNull()
        assertThat(exists).isFalse()
    }

    @Test
    fun `should find existing users`(@Autowired repository: DelegatingR2dbcUserRepository) = runBlocking<Unit> {
        repository.create(CreateUser(username = "username1", email = "email1", firstName = "firstName1", lastName = "lastName1")).id
        repository.create(CreateUser(username = "username2", email = "email2", firstName = "firstName2", lastName = "lastName2")).id

        assertThat(findAll(repository, FindUser(username = "username1"))).hasSize(1)
        assertThat(findAll(repository, FindUser(username = null))).hasSize(2)
    }

    @Test
    fun `should delete existing users`(@Autowired repository: DelegatingR2dbcUserRepository) = runBlocking<Unit> {
        repository.create(CreateUser(username = "username1", email = "email1", firstName = "firstName1", lastName = "lastName1")).id
        repository.create(CreateUser(username = "username2", email = "email2", firstName = "firstName2", lastName = "lastName2")).id
        repository.create(CreateUser(username = "username3", email = "email3", firstName = "firstName3", lastName = "lastName3")).id
        assertThat(findAll(repository, FindUser())).hasSize(3)

        repository.deleteAll(FindUser(username = "username3"))
        assertThat(findAll(repository, FindUser())).hasSize(2)

        repository.deleteAll(FindUser())
        assertThat(findAll(repository, FindUser())).hasSize(0)
    }

    private suspend fun findAll(repository: DelegatingR2dbcUserRepository, request: FindUser): MutableList<User> = repository.findAll(request).asFlux().collectList().block()!!
}