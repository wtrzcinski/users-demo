package com.codete.user.periphery.r2dbc

import org.springframework.data.r2dbc.repository.R2dbcRepository

internal interface R2dbcUserPatchRepository: R2dbcRepository<R2dbcUserPatch?, Long?>