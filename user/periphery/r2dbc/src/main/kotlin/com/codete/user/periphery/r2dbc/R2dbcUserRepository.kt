package com.codete.user.periphery.r2dbc

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.lang.Nullable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal interface R2dbcUserRepository: R2dbcRepository<R2dbcUser?, Long?> {
    fun findAllByUsername(@Nullable username: String?): Flux<R2dbcUser>

    fun deleteAllByUsername(@Nullable username: String?): Mono<Void>
}