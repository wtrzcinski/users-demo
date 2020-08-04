package com.codete.user.core.model.operation

import com.codete.user.core.common.StringUtils.isEmpty
import com.codete.user.core.common.StringUtils.isValidEmail
import com.codete.user.core.model.UserId

sealed class Operation

data class CreateUser(
        val username: String?,
        val email: String?,
        val password: String? = null,
        val firstName: String? = null,
        val lastName: String? = null
) : Operation() {
    fun validate(): String? {
        if (isEmpty(username)) {
            return "'username' field is required"
        }
        if (isEmpty(password)) {
            return "'password' field is required"
        }
        if (isEmpty(email)) {
            return "'email' field is required"
        }
        if (!isValidEmail(email)) {
            return "Email is not valid '$email'"
        }

        return null;
    }
}

data class PatchUser(
        val id: UserId? = null,
        val password: String? = null,
        val firstName: String? = null,
        val lastName: String? = null
) : Operation() {
    fun validate(): String? {
        if (id == null || isEmpty(id.id)) {
            return "'id' field not provided"
        }

        if (isEmpty(password) && isEmpty(firstName) && isEmpty(lastName)) {
            return "No field to update (updatable fields: 'password', 'firstName', 'lastName')"
        }

        return null
    }
}

data class DeleteUser(val id: UserId) : Operation()

data class FindUser(val username: String? = null)
