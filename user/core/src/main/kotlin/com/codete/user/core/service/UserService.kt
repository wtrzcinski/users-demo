package com.codete.user.core.service

import com.codete.user.core.model.CreateNotificationRequest
import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.DeleteUser
import com.codete.user.core.model.operation.FindUser
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.core.repository.Encryptor
import com.codete.user.core.repository.Notifications
import com.codete.user.core.repository.UserRepository
import com.codete.user.core.service.exception.DuplicateException
import com.codete.user.core.service.exception.NotFoundException
import com.codete.user.core.service.exception.ValidationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

open class UserService(
        private val userRepository: UserRepository,
        private val notifications: Notifications,
        private val encryptor: Encryptor
) {

    open suspend fun create(request: CreateUser): User {
//        validate
        val validate = request.validate()
        if (validate != null) {
            throw ValidationException(validate)
        }

//        check if exists
        if (userRepository.existsByUsername(request.username!!)) {
            throw DuplicateException("User with name '${request.username}' already exists")
        }

//        create
        val user = userRepository.create(request.copy(password = encryptor.encrypt(request.password)))
        notifications.accountCreated(CreateNotificationRequest(user))
        return user
    }

    open suspend fun patch(request: PatchUser): User? {
//        validate
        val validate = request.validate()
        if (validate != null) {
            throw ValidationException(validate)
        }

//        check if exists
        if (!userRepository.exists(request.id!!)) {
            throw NotFoundException("User with id '${request.id.id}' does not exist")
        }

//        update
        userRepository.update(request.copy(password = encryptor.encrypt(request.password)))
        return userRepository.findById(request.id)
    }

    open suspend fun delete(request: DeleteUser): UserId {
//        check if exists
        if (!userRepository.exists(request.id)) {
            throw NotFoundException("User with id '${request.id.id}' does not exist")
        }

//        delete
        userRepository.delete(request.id)
        return request.id
    }

    open suspend fun deleteAll(request: FindUser): FindUser {
        userRepository.deleteAll(request)
        return request
    }

    @FlowPreview
    open suspend fun findAll(request: FindUser): Flow<User> {
        return userRepository.findAll(request)
    }
}