package net.otuskotlin.ingredientscan.api.log1.mapper

import java.time.Instant
import java.time.ZoneOffset
import net.otuskotlin.ingredientscan.api.log1.models.*
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
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


fun InternalContext.toLog(logId: String) = CommonLogModel(
    messageTime = Instant.now().atOffset(ZoneOffset.UTC),
    logId = logId,
    source = CommonLogModel.Source.INTERNAL_API,
    scan = toIsScanLog(),
    errors = errors.map { it.toLog() }.takeIf { it.isNotEmpty() },
    performance = null
)

private fun InternalCommand.toLogOperation(): ScanLogOperation? =
    ScanLogOperation.entries.firstOrNull { it.value.equals(this.name.lowercase(), ignoreCase = true) }


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

private fun InternalContext.toIsScanLog(): IngredientScanLogModel? {
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
        componentsCount = components.size.takeIf { it > 0 },
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
    IsColor.VERY_DARK_RED -> AnalysisLog.Color.VERY_DARK_RED
    IsColor.DARK_RED -> AnalysisLog.Color.DARK_RED
    IsColor.DEEP_RED -> AnalysisLog.Color.DEEP_RED
    IsColor.RED -> AnalysisLog.Color.RED
    IsColor.LIGHT_RED -> AnalysisLog.Color.LIGHT_RED
    IsColor.RED_ORANGE -> AnalysisLog.Color.RED_ORANGE
    IsColor.ORANGE -> AnalysisLog.Color.ORANGE
    IsColor.LIGHT_ORANGE -> AnalysisLog.Color.LIGHT_ORANGE
    IsColor.DARK_YELLOW -> AnalysisLog.Color.DARK_YELLOW
    IsColor.YELLOW -> AnalysisLog.Color.YELLOW
    IsColor.LIGHT_YELLOW -> AnalysisLog.Color.LIGHT_YELLOW
    IsColor.YELLOW_GREEN -> AnalysisLog.Color.YELLOW_GREEN
    IsColor.PALE_GREEN -> AnalysisLog.Color.PALE_GREEN
    IsColor.LIGHT_GREEN -> AnalysisLog.Color.LIGHT_GREEN
    IsColor.GREEN -> AnalysisLog.Color.GREEN
    IsColor.MEDIUM_GREEN -> AnalysisLog.Color.MEDIUM_GREEN
    IsColor.BRIGHT_GREEN -> AnalysisLog.Color.BRIGHT_GREEN
    IsColor.VIBRANT_GREEN -> AnalysisLog.Color.VIBRANT_GREEN
    IsColor.FRESH_GREEN -> AnalysisLog.Color.FRESH_GREEN
    IsColor.BRILLIANT_GREEN -> AnalysisLog.Color.BRILLIANT_GREEN
    IsColor.NONE -> null
}

private fun IsLogLevel.toLogLevel(): ErrorLogModel.Level = when (this) {
    IsLogLevel.ERROR -> ErrorLogModel.Level.ERROR
    IsLogLevel.WARN -> ErrorLogModel.Level.WARN
    IsLogLevel.INFO -> ErrorLogModel.Level.INFO
    IsLogLevel.DEBUG -> ErrorLogModel.Level.DEBUG
    IsLogLevel.TRACE -> ErrorLogModel.Level.DEBUG
}