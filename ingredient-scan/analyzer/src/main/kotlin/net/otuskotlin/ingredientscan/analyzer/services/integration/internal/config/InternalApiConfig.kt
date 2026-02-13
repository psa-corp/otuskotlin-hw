package net.otuskotlin.ingredientscan.analyzer.services.integration.internal.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(InternalApiProperties::class)
open class InternalApiConfig(private val internalApiProperties: InternalApiProperties) {

    @Bean("internalWebClient")
    open fun internalWebClient(
        builder: WebClient.Builder,
        internalApiProperties: InternalApiProperties
    ): WebClient {
        return builder
            .baseUrl(internalApiProperties.url)
            .defaultHeader(internalApiProperties.header, internalApiProperties.token)
            .build()
    }
}
