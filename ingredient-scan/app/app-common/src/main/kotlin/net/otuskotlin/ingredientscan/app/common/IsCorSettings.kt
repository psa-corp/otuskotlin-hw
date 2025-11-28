package net.otuskotlin.ingredientscan.app.common

import net.otuskotlin.ingredientscan.libs.logging.common.IsLoggerProvider

data class IsCorSettings(
    val loggerProvider: IsLoggerProvider = IsLoggerProvider(),
) {
    companion object {
        val NONE = IsCorSettings()
    }
}