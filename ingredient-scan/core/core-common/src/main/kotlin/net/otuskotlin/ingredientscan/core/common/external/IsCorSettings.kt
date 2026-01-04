package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.logging.IsLoggerProvider

data class IsCorSettings(
    val loggerProvider: IsLoggerProvider = IsLoggerProvider(),
) {
    companion object {
        val NONE = IsCorSettings()
    }
}