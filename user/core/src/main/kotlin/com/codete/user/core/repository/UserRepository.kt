package com.codete.user.core.repository

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.FindUser
import com.codete.user.core.model.operation.PatchUser
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun create(user: CreateUser): User

    suspend fun findById(id: UserId): User?

    suspend fun exists(id: UserId): Boolean

    suspend fun existsByUsername(username: String): Boolean

    suspend fun update(user: PatchUser)

    suspend fun delete(id: UserId)

    @FlowPreview
    suspend fun findAll(request: FindUser): Flow<User>

    suspend fun deleteAll(request: FindUser)

}