package com.codete.notification.entrypoint.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
internal class WebfluxAutoConfiguration: WebFluxConfigurer {
    @Bean
    fun docket(@Value("\${swagger.host:}") host: String?, @Value("\${swagger.scheme:}") scheme: String?): Docket {
        var docket = Docket(DocumentationType.OAS_30)

        if (host != null && host.isNotBlank()) {
            docket = docket.host(host)
        }

        if (scheme != null && scheme.isNotBlank()) {
            docket = docket.protocols(setOf(scheme))
        }

        return docket
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
                .build()
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false)
    }
}