package net.otuskotlin.ingredientscan.scanner.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.internal")
data class InternalSecurityProperties(
    var token: String = "default-secret",
    var prefix: String = "/v1/internal",
    var header: String = "X-Internal-Secret"
)