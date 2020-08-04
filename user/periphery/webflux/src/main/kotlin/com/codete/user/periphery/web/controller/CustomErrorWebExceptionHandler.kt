package com.codete.user.periphery.web.controller

import com.codete.user.core.service.exception.DuplicateException
import com.codete.user.core.service.exception.NotFoundException
import com.codete.user.core.service.exception.ServiceException
import com.codete.user.core.service.exception.ValidationException
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.StringUtils.hasText
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

internal class CustomErrorWebExceptionHandler(
        private val errorAttributes: ErrorAttributes,
        resourceProperties: ResourceProperties,
        errorProperties: ErrorProperties,
        applicationContext: ApplicationContext
) : DefaultErrorWebExceptionHandler(errorAttributes, resourceProperties, errorProperties, applicationContext) {

    override fun renderErrorResponse(request: ServerRequest?): Mono<ServerResponse?>? {
        val error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL))
        val exception = errorAttributes.getError(request)
        val response = createResponseBody(exception, error)
        return ServerResponse.status(response.status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response.content!!))
    }

    companion object {
        fun createResponseBody(exception: Throwable, error: MutableMap<String?, Any?> = mutableMapOf()): ResultWithStatus<MutableMap<String?, Any?>> {
            val httpStatus = getHttpStatus(exception, error)
            error["status"] = httpStatus.value()
            error["error"] = httpStatus.reasonPhrase
            if (!hasText(error["message"] as String?)) {
                error["message"] = exception.message
            }
            return ResultWithStatus(httpStatus.value(), error)
        }

        private fun getHttpStatus(exception: Throwable, errorAttributes: Map<String?, Any?>): HttpStatus {
            if (exception is ServiceException) {
                return toHttpStatus(exception)
            }
            val cause = exception.cause
            if (cause is ServiceException) {
                return toHttpStatus(cause)
            }
            return HttpStatus.valueOf(errorAttributes["status"] as Int)
        }

        private fun toHttpStatus(exception: ServiceException): HttpStatus {
            return when (exception) {
                is NotFoundException -> HttpStatus.NOT_FOUND
                is DuplicateException -> HttpStatus.CONFLICT
                is ValidationException -> HttpStatus.BAD_REQUEST
            }
        }
    }
}