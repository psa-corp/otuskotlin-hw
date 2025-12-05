package net.otuskotlin.ingredientscan.api.log1.mapper

import java.time.Instant
import java.time.ZoneOffset
import net.otuskotlin.ingredientscan.api.log1.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsRequestId
import net.otuskotlin.ingredientscan.core.common.logging.IsLogLevel

fun IsContext.toLog(logId: String) = CommonLogModel(
    messageTime = Instant.now().atOffset(ZoneOffset.UTC),
    logId = logId,
    source = CommonLogModel.Source.EXTERNAL_API,
    scan = toIsScanLog(),
    errors = errors.map { it.toLog() }.takeIf { it.isNotEmpty() },
    performance = null
)
private fun IsCommand.toLogOperation(): ScanLogOperation? =
    ScanLogOperation.entries.firstOrNull { it.value.equals(this.name.lowercase(), ignoreCase = true) }

private fun IsContext.toIsScanLog(): IngredientScanLogModel? {
    val analysisNone = IsAnalysis.NONE
    val compositionNone = IsComposition.NONE

    return IngredientScanLogModel(
        requestId = requestId.takeIf { it != IsRequestId.NONE }?.asString(),
        operation = command.toLogOperation(),

        // Request
        requestAnalysis = analysisRequest.takeIf { it != analysisNone }?.toLog(),
        requestComposition = compositionRequest.takeIf { it != compositionNone }?.toLog(),

        // Response
        responseAnalysis = analysisResponse.takeIf { it != analysisNone }?.toLog(),
        responseComposition = compositionResponse.takeIf { it != compositionNone }?.toLog(),
    ).takeIf { it != IngredientScanLogModel() }
}

// --- Analysis and Composition Mappers ---

private fun IsAnalysis.toLog(): AnalysisLog? {
    val analysisNone = IsAnalysisId.NONE
    // Возвращаем null, если объект пустой (не имеет ID)
    if (this.id == analysisNone) return null

    return AnalysisLog(
        id = id.asString(),
        compositionId = compositionId.takeIf { it != IsCompositionId.NONE }?.asString(),
        rating = rating.takeIf { it > 0 }?.toDouble(),
        color = color.toLogColor(),
        problematicComponentsCount = problematicComponents.size.takeIf { it > 0 },
        safeComponentsCount = safeComponents.size.takeIf { it > 0 },
        regenerationCount = null
    )
}

private fun IsComposition.toLog() = CompositionLog(
    id = id.takeIf { it != IsCompositionId.NONE }?.asString(),
    textLength = text.length.takeIf { it > 0 },
    createDate = createDate.atOffset(ZoneOffset.UTC),
)


// --- Error Mapper ---

private fun IsError.toLog() = ErrorLogModel(
    message = message.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    code = code.takeIf { it.isNotBlank() },
    level = level.toLogLevel(),
)

private fun IsColor.toLogColor(): AnalysisLog.Color? = when (this) {
    IsColor.DARK_RED -> AnalysisLog.Color.DARK_RED
    IsColor.RED -> AnalysisLog.Color.RED
    IsColor.ORANGE -> AnalysisLog.Color.ORANGE
    IsColor.YELLOW -> AnalysisLog.Color.YELLOW
    IsColor.LIGHT_YELLOW -> AnalysisLog.Color.LIGHT_YELLOW
    IsColor.LIGHT_GREEN -> AnalysisLog.Color.LIGHT_GREEN
    IsColor.GREEN -> AnalysisLog.Color.GREEN
    IsColor.DARK_GREEN -> AnalysisLog.Color.DARK_GREEN
    IsColor.NONE -> null
}

private fun IsLogLevel.toLogLevel(): ErrorLogModel.Level = when (this) {
    IsLogLevel.ERROR -> ErrorLogModel.Level.ERROR
    IsLogLevel.WARN -> ErrorLogModel.Level.WARN
    IsLogLevel.INFO -> ErrorLogModel.Level.INFO
    IsLogLevel.DEBUG -> ErrorLogModel.Level.DEBUG
    IsLogLevel.TRACE -> ErrorLogModel.Level.DEBUG
}