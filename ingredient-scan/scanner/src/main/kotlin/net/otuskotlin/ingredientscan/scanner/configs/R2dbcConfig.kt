package net.otuskotlin.ingredientscan.scanner.configs

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["net.otuskotlin.ingredientscan.scanner.repositories"])
open class R2dbcConfig {

    @Bean
    @Primary
    open fun databaseClient(
        connectionFactory: ConnectionFactory
    ): DatabaseClient = DatabaseClient.builder()
        .connectionFactory(connectionFactory)
        .build()
}