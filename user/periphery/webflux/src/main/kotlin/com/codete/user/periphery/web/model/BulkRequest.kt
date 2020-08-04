package com.codete.user.periphery.web.model

import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.CreateUser
import com.codete.user.core.model.operation.DeleteUser
import com.codete.user.core.model.operation.PatchUser
import com.codete.user.core.model.operation.Operation
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = BulkPatchUserRequest::class, name = "patch"),
        JsonSubTypes.Type(value = BulkCreateUserRequest::class, name = "create"),
        JsonSubTypes.Type(value = BulkDeleteUserRequest::class, name = "delete")
)
@ApiModel(subTypes = [
    BulkCreateUserRequest::class,
    BulkDeleteUserRequest::class,
    BulkPatchUserRequest::class
])
sealed class BulkRequest<T : Operation> {
    abstract fun content(): T

    @ApiModelProperty(value = "Type of operation", example = "patch|create|delete")
    var type: String? = null
}

@ApiModel(parent = BulkRequest::class)
class BulkCreateUserRequest : BulkRequest<CreateUser>() {
    var username: String? = null
    var email: String? = null
    var password: String? = null
    var firstName: String? = null
    var lastName: String? = null

    override fun content(): CreateUser {
        return CreateUser(
                username = username ?: "",
                email = email ?: "",
                password = password ?: "",
                firstName = firstName,
                lastName = lastName
        )
    }
}

@ApiModel(parent = BulkRequest::class)
class BulkDeleteUserRequest : BulkRequest<DeleteUser>() {
    var id: String? = null

    override fun content(): DeleteUser {
        return DeleteUser(UserId(id ?: ""))
    }
}

@ApiModel(parent = BulkRequest::class)
class BulkPatchUserRequest : BulkRequest<PatchUser>() {
    var id: String? = null
    var password: String? = null
    var firstName: String? = null
    var lastName: String? = null

    override fun content(): PatchUser {
        return PatchUser(
                id = UserId(id ?: ""),
                password = password,
                firstName = firstName,
                lastName = lastName
        )
    }
}