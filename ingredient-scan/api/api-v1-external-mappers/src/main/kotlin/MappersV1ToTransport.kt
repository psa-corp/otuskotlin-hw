package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsRiskLevel
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownIsCommand
import java.time.ZoneOffset

fun IsContext.toTransport(): IResponse = when (val cmd = command) {
    IsCommand.ANALYSIS_GET -> toTransportAnalysisGet()
    IsCommand.ANALYSIS_REGENERATE -> toTransportAnalysisRegenerate()
    IsCommand.COMPOSITION_CREATE_MANUAL -> toTransportCompositionCreateManual()
    IsCommand.COMPOSITION_CREATE_PHOTOS -> toTransportCompositionCreatePhotos()
    IsCommand.COMPOSITION_GET -> toTransportCompositionGet()
    IsCommand.DOWNLOAD_FILE -> toTransportDownloadFile() // Обычно файлы отдаются стримом, но если есть JSON ответ при ошибке
    IsCommand.NONE -> throw UnknownIsCommand(cmd)

    else -> throw UnknownIsCommand(cmd)
}

// --- Analysis Responses ---

fun IsContext.toTransportAnalysisGet() = AnalysisGetResponse(
    responseType = "analysisGet",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    analysis = analysisResponse.toTransport()
)

fun IsContext.toTransportAnalysisRegenerate() = AnalysisRegenerateResponse(
    responseType = "analysisRegenerate",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    analysis = analysisResponse.toTransport()
)

// --- Composition Responses ---

fun IsContext.toTransportCompositionCreateManual() = CompositionCreateByManualResponse(
    responseType = "compositionCreateByManual",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    compositionId = compositionResponse.id.takeIf { it != IsCompositionId.NONE }?.asString()
)

fun IsContext.toTransportCompositionCreatePhotos() = CompositionCreateByPhotosResponse(
    responseType = "compositionCreateByPhotos",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    compositionId = compositionResponse.id.takeIf { it != IsCompositionId.NONE }?.asString()
)

fun IsContext.toTransportCompositionGet() = CompositionGetResponse(
    responseType = "compositionGet",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    composition = compositionResponse.toTransport()
)

// --- File/Error Responses ---

fun IsContext.toTransportDownloadFile(): IResponse {
    // Обычно для скачивания файла возвращается поток байтов контроллером,
    // но если произошла ошибка, мы возвращаем JSON.
    return ErrorResponse(
        responseType = "error",
        result = ResponseResult.ERROR,
        errors = errors.toTransportErrors()
    )
}

// --- Data Object Mappers ---

fun IsAnalysis.toTransport(): Analysis? = if (this.isEmpty()) null else Analysis(
    id = id.takeIf { it != IsAnalysisId.NONE }?.asString(),
    compositionId = compositionId.takeIf { it != IsCompositionId.NONE }?.asString(),
    createDate = createDate.atOffset(ZoneOffset.UTC),
    description = description.takeIf { it.isNotBlank() },
    rating = rating.takeIf { it > 0 }?.toDouble(), // Предполагаем rating > 0
    color = color.toTransport(),
    problematicComponent = problematicComponents.map { it.toTransport() },
    safeComponent = safeComponents.map { it.toTransport() }
)

fun IsComposition.toTransport(): Composition? = if (this.id == IsCompositionId.NONE) null else Composition(
    id = id.asString(),
    createDate = createDate.atOffset(ZoneOffset.UTC),
    text = text.takeIf { it.isNotBlank() },
    // analysisId и useCount нужно добавить в IsComposition или брать из других мест, если они там есть
)

fun IsComponent.toTransport(): Component = Component(
    id = id.asString(),
    name = name.takeIf { it.isNotBlank() },
    createDate = createDate.atOffset(ZoneOffset.UTC),
    scientificName = scientificName.takeIf { it.isNotBlank() },
    description = description.takeIf { it.isNotBlank() },
    sources = sources.takeIf { it.isNotBlank() },
    riskLevel = riskLevel.toTransport(),
    healthRisks = healthRisks.takeIf { it.isNotBlank() }
)

// --- Enum Mappers ---

private fun IsColor.toTransport(): Analysis.Color? = when (this) {
    IsColor.DARK_RED -> Analysis.Color.DARK_RED
    IsColor.RED -> Analysis.Color.RED
    IsColor.ORANGE -> Analysis.Color.ORANGE
    IsColor.YELLOW -> Analysis.Color.YELLOW
    IsColor.LIGHT_YELLOW -> Analysis.Color.LIGHT_YELLOW
    IsColor.LIGHT_GREEN -> Analysis.Color.LIGHT_GREEN
    IsColor.GREEN -> Analysis.Color.GREEN
    IsColor.DARK_GREEN -> Analysis.Color.DARK_GREEN
    IsColor.NONE -> null
}

private fun IsRiskLevel.toTransport(): RiskLevel? = when (this) {
    IsRiskLevel.CRITICAL -> RiskLevel.CRITICAL
    IsRiskLevel.HIGH -> RiskLevel.HIGH
    IsRiskLevel.MEDIUM -> RiskLevel.MEDIUM
    IsRiskLevel.LOW -> RiskLevel.LOW
    IsRiskLevel.MINIMAL -> RiskLevel.MINIMAL
    IsRiskLevel.NONE -> null
}

private fun IsState.toResult(): ResponseResult = when (this) {
    IsState.RUNNING, IsState.FINISHING -> ResponseResult.SUCCESS
    IsState.FAILING -> ResponseResult.ERROR
    IsState.NONE -> ResponseResult.ERROR
}

// --- Error Mappers ---

private fun List<IsError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .toList()
    .takeIf { it.isNotEmpty() }

private fun IsError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)