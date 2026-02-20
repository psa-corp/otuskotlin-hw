package net.otuskotlin.ingredientscan.scanner.configs

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(InternalSecurityProperties::class)
open class InternalSecurityConfig(private val internalSecurityProperties: InternalSecurityProperties)  {
}