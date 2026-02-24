package net.otuskotlin.ingredientscan.analyzer.services.integration.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.integration.ai")
data class AIApiProperties(
    var token: String = "default-secret",
    var url: String = "http://localhost:5000/v1",
    var header: String = "X-Internal-Secret"
)