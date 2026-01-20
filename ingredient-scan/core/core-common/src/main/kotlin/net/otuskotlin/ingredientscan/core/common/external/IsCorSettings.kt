package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.IsContentProvider
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.common.logging.IsLoggerProvider

data class IsCorSettings(
    val loggerProvider: IsLoggerProvider = IsLoggerProvider(),
    val messageSender: IsMessageSender?,
    val contextRepository: IsContextRepository?,
    val contentProvider: IsContentProvider?,
) {
    companion object {
        val NONE = IsCorSettings(messageSender = null, contextRepository = null, contentProvider = null)
    }
}