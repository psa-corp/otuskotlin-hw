package net.otuskotlin.ingredientscan.analyzer.services.integration.ai.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(AIApiProperties::class)
open class AIApiConfig(private val aiApiProperties: AIApiProperties) {

    @Bean("aiWebClient")
    open fun internalWebClient(
        builder: WebClient.Builder,
        internalApiProperties: AIApiProperties
    ): WebClient {
        return builder
            .baseUrl(internalApiProperties.url)
            .defaultHeader(internalApiProperties.header, internalApiProperties.token)
            .build()
    }
}
