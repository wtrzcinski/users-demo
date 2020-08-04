package com.codete.user.periphery.r2dbc

import com.codete.user.periphery.r2dbc.common.R2dbcPatch
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class R2dbcUserPatch(
        @field:Id
        @field:Column("id")
        var id: Long? = null,

        @field:Column("password")
        var password: String? = null,

        @field:Column("first_name")
        var firstName: String? = null,

        @field:Column("last_name")
        var lastName: String? = null
) : R2dbcPatch