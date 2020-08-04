package com.codete.user.periphery.r2dbc.config

import com.codete.user.core.repository.UserRepository
import com.codete.user.periphery.r2dbc.R2dbcUserPatchRepository
import com.codete.user.periphery.r2dbc.R2dbcUserRepository
import com.codete.user.periphery.r2dbc.DelegatingR2dbcUserRepository
import com.codete.user.periphery.r2dbc.common.R2dbcPatch
import io.r2dbc.spi.ConnectionFactory
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.CustomConversions
import org.springframework.data.mapping.PersistentPropertyAccessor
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.SettableValue
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import java.util.*

@Configuration
@EnableR2dbcRepositories("com.codete.user.periphery.r2dbc")
@EnableConfigurationProperties(LiquibaseProperties::class)
internal class R2dbcAutoConfiguration {
    @Bean
    fun springDataUserRepository(r2dbcRepository: R2dbcUserRepository, patchRepository: R2dbcUserPatchRepository): UserRepository {
        return DelegatingR2dbcUserRepository(r2dbcRepository, patchRepository)
    }

    /**
     * org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.LiquibaseConfiguration#liquibase()
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.liquibase", name = ["url"])
    fun springLiquibase(properties: LiquibaseProperties): SpringLiquibase {
        val liquibase = DataSourceClosingSpringLiquibase()
        liquibase.dataSource = DataSourceBuilder.create()
                .url(properties.url)
                .username(properties.user)
                .password(properties.password)
                .build()
        liquibase.changeLog = properties.changeLog
        liquibase.contexts = properties.contexts
        liquibase.defaultSchema = properties.defaultSchema
        liquibase.liquibaseSchema = properties.liquibaseSchema
        liquibase.liquibaseTablespace = properties.liquibaseTablespace
        liquibase.databaseChangeLogTable = properties.databaseChangeLogTable
        liquibase.databaseChangeLogLockTable = properties.databaseChangeLogLockTable
        liquibase.isDropFirst = properties.isDropFirst
        liquibase.setShouldRun(properties.isEnabled)
        liquibase.labels = properties.labels
        liquibase.setChangeLogParameters(properties.parameters)
        liquibase.setRollbackFile(properties.rollbackFile)
        liquibase.isTestRollbackOnUpdate = properties.isTestRollbackOnUpdate
        return liquibase
    }

    /**
     * @see org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration.r2dbcCustomConversions
     */
    @Bean
    fun r2dbcCustomConversions(connectionFactory: ConnectionFactory, mappingContext: ObjectFactory<RelationalMappingContext>): R2dbcCustomConversions? {
        val dialect = DialectResolver.getDialect(connectionFactory)
        val converters: MutableList<Any> = ArrayList(dialect.converters)
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)
        return R2dbcCustomConversions(
                CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters),
                ownConverters(mappingContext)
        )
    }

    fun ownConverters(mappingContext: ObjectFactory<RelationalMappingContext>): MutableList<Any> {
        return mutableListOf(R2dbcPatchConverter(mappingContextProvider = mappingContext))
    }

    class R2dbcPatchConverter(private val mappingContextProvider: ObjectFactory<RelationalMappingContext>) : Converter<R2dbcPatch, OutboundRow> {
        /**
         * @see org.springframework.data.r2dbc.convert.MappingR2dbcConverter.write
         */
        override fun convert(source: R2dbcPatch): OutboundRow {
            val mappingContext = mappingContextProvider.`object`
            val entity = mappingContext.getRequiredPersistentEntity(source::class.java)
            val propertyAccessor: PersistentPropertyAccessor<*> = entity.getPropertyAccessor<Any>(source)

            val sink = OutboundRow()
            for (property in entity) {
                if (property.isWritable) {
                    val value = propertyAccessor.getProperty(property)
                    if (value != null) {
                        sink[property.columnName] = SettableValue.from(value)
                    }
                }
            }
            return sink
        }
    }
}