package com.codete.user.periphery.web.config

import com.codete.user.core.model.User
import com.codete.user.core.model.UserId
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
internal class ObjectMapperConfig {
    interface UserMixin {
        @JsonUnwrapped
        fun getId(): UserId
    }

    @Autowired
    fun configureObjectMapper(mapper: ObjectMapper) {
        mapper.addMixIn(User::class.java, UserMixin::class.java)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}