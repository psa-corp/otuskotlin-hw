package net.otuskotlin.ingredientscan.analyzer.services.integration.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.integration.internal")
data class InternalApiProperties(
    var token: String = "default-secret",
    var url: String = "http://localhost:8081/v1",
    var header: String = "X-Internal-Secret"
)