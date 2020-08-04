package com.codete.user.periphery.r2dbc

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.FindUser
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.exception.ValidationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull

internal class DelegatingR2dbcUserRepository(
        private val repository: R2dbcUserRepository,
        private val patchRepository: R2dbcUserPatchRepository
) : UserRepository {

    override suspend fun exists(id: UserId): Boolean {
        return repository.existsById(toLong(id)).awaitFirst()
    }

    override suspend fun existsByUsername(username: String): Boolean {
        return repository.findAllByUsername(username)
                .collectList()
                .map { it.size > 0 }
                .awaitFirst()
    }

    override suspend fun create(user: CreateUser): User {
        return repository.save(toR2dbcUser(user)).map { toUser(it) }.awaitFirst()
    }

    override suspend fun update(user: PatchUser) {
        patchRepository.save(toR2dbcUserPatch(user)).awaitFirstOrNull()
    }

    override suspend fun delete(id: UserId) {
        repository.deleteById(toLong(id)).awaitFirstOrNull()
    }

    @FlowPreview
    override suspend fun findAll(request: FindUser): Flow<User> {
        return if (request.username == null) {
            repository.findAll().map { toUser(it) }
        } else {
            repository.findAllByUsername(request.username!!).map { toUser(it) }
        }.asFlow()
    }

    override suspend fun findById(id: UserId): User? {
        return repository.findById(toLong(id)).map { toUser(it) }.awaitFirstOrNull()
    }

    override suspend fun deleteAll(request: FindUser) {
        val delete = if (request.username == null) {
            repository.deleteAll()
        } else {
            repository.deleteAllByUsername(request.username!!)
        }
        delete.awaitFirstOrNull()
    }

    private fun toUser(it: R2dbcUser?): User {
        return User(id = UserId(it!!.id.toString()), username = it.username!!, email = it.email!!, firstName = it.firstName, lastName = it.lastName)
    }

    private fun toR2dbcUser(it: CreateUser?): R2dbcUser {
        return R2dbcUser(username = it!!.username, email = it.email, password = it.password, firstName = it.firstName, lastName = it.lastName)
    }

    private fun toR2dbcUserPatch(it: PatchUser?): R2dbcUserPatch {
        return R2dbcUserPatch(id = toLong(it!!.id!!), password = it.password, firstName = it.firstName, lastName = it.lastName)
    }

    private fun toLong(it: UserId): Long {
        try {
            return it.id.toLong()
        } catch (ex: NumberFormatException) {
            throw ValidationException("Incorrect id '${it.id}'")
        }
    }
}