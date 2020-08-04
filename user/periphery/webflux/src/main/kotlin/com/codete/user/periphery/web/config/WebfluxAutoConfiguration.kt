package com.codete.user.periphery.web.config

import com.codete.user.periphery.web.controller.CustomErrorWebExceptionHandler
import com.codete.user.periphery.web.controller.UserController
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.view.ViewResolver
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.stream.Collectors

@Configuration
@EnableSwagger2
@Import(ObjectMapperConfig::class)
@ComponentScan(basePackageClasses = [UserController::class])
internal class WebfluxAutoConfiguration(private val serverProperties: ServerProperties) : WebFluxConfigurer {

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

    /**
     * @see org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration.errorWebExceptionHandler
     */
    @Bean
    @Order(-2)
    fun customErrorWebExceptionHandler(
            errorAttributes: ErrorAttributes,
            resourceProperties: ResourceProperties,
            viewResolvers: ObjectProvider<ViewResolver?>,
            serverCodecConfigurer: ServerCodecConfigurer,
            applicationContext: ApplicationContext
    ): ErrorWebExceptionHandler {
        val exceptionHandler = CustomErrorWebExceptionHandler(errorAttributes, resourceProperties, this.serverProperties.getError(), applicationContext)
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()))
        exceptionHandler.setMessageWriters(serverCodecConfigurer.writers)
        exceptionHandler.setMessageReaders(serverCodecConfigurer.readers)
        return exceptionHandler
    }
}