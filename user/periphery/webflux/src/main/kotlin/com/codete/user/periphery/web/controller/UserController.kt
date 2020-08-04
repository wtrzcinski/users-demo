package com.codete.user.periphery.web.controller

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.codete.user.core.model.operation.*
import com.codete.user.core.service.UserService
import com.codete.user.core.service.exception.ServiceException
import com.codete.user.periphery.web.model.BulkRequest
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("user")
internal class UserController(private val userService: UserService) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @ApiOperation(value = "Creates user")
    @ApiResponses(
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "409", description = "Conflict")
    )
    @PostMapping
    @ResponseStatus(CREATED)
    suspend fun create(@RequestBody request: CreateUser): User {
        return userService.create(request)
    }

    @ApiOperation(value = "Finds users")
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "400", description = "Bad Request")
    )
    @GetMapping
    @FlowPreview
    @ResponseStatus(OK)
    suspend fun findAll(request: FindUser): Flow<User> {
        return userService.findAll(request)
    }

    @ApiOperation(value = "Updates one user")
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "404", description = "Not Found")
    )
    @PatchMapping("{id}")
    @ResponseStatus(OK)
    suspend fun update(@PathVariable id: String, @ApiParam(example = "{firstName: string, lastName: string, password: string}") @RequestBody request: PatchUser): User? {
        return userService.patch(request.copy(id = UserId(id)))
    }

    @ApiOperation(value = "Deletes all users")
    @ApiResponses(
            ApiResponse(responseCode = "204", description = "No Content"),
            ApiResponse(responseCode = "400", description = "Bad Request")
    )
    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    suspend fun deleteAll(request: FindUser): FindUser {
        return userService.deleteAll(request)
    }

    @ApiOperation(value = "Deletes one user")
    @ApiResponses(
            ApiResponse(responseCode = "204", description = "No Content"),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "404", description = "Not Found")
    )
    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    suspend fun deleteOne(@PathVariable id: String): UserId? {
        return userService.delete(DeleteUser(UserId(id)))
    }

    @FlowPreview
    @ApiOperation(value = "Updates multiple users")
    @ApiResponses(
            ApiResponse(responseCode = "207", description = "Multi-Status")
    )
    @PostMapping("/bulk")
    @ResponseStatus(MULTI_STATUS)
    suspend fun bulk(@RequestBody request: List<BulkRequest<out Operation>>): List<ResultWithStatus<out Any>> {
        return request.map { it.content() }.map {
            try {
                when (it) {
                    is CreateUser -> ResultWithStatus(CREATED.value(), userService.create(it))
                    is PatchUser -> ResultWithStatus(OK.value(), userService.patch(it))
                    is DeleteUser -> ResultWithStatus(NO_CONTENT.value(), userService.delete(it))
                }
            } catch (ex: ServiceException) {
                return@map CustomErrorWebExceptionHandler.createResponseBody(ex)
            }
        }
    }

}