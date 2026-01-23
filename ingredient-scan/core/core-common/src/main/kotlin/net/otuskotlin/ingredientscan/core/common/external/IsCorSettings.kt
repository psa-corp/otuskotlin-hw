package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContentProvider
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.common.logging.IsLoggerProvider

data class IsCorSettings(
    val loggerProvider: IsLoggerProvider = IsLoggerProvider(),
    val messageSender: IsMessageSender?,
    val contentProvider: IsContentProvider?,
    val contextRepository: IsContextRepository?,
    val compositionRepository: IsCompositionRepository?,
    val analysisRepository: IsAnalysisRepository?,
) {
    companion object {
        val NONE = IsCorSettings(
            messageSender = null,
            contentProvider = null,
            contextRepository = null,
            compositionRepository = null,
            analysisRepository = null
        )
    }
}