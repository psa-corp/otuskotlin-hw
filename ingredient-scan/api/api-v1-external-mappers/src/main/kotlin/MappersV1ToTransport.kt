package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownIsCommand
import java.time.ZoneOffset

// --- Analysis Responses ---

fun IsContext.toTransportAnalysisGet() = AnalysisGetResponse(
    responseType = "analysisGet",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    analysis = analysisResponse.toTransport()
)

fun IsContext.toTransportAnalysisCreate() = AnalysisCreateResponse(
    responseType = "analysisCreate",
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
    contextId = id.takeIf { it != IsContextId.NONE }?.asString(),
    composition = compositionResponse.takeIf { it != IsComposition.NONE }?.toTransport()
)

fun IsContext.toTransportCompositionCreatePhotos() = CompositionCreateByPhotosResponse(
    responseType = "compositionCreateByPhotos",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    contextId = id.takeIf { it != IsContextId.NONE }?.asString(),
    composition = compositionResponse.takeIf { it != IsComposition.NONE }?.toTransport()
)

fun IsContext.toTransportCompositionContextGet() = CompositionContextGetResponse(
    responseType = "compositionContextGet",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    context = compositionContextResponse.toTransport()
)

fun IsContext.toTransportCompositionGet() = CompositionGetResponse(
    responseType = "compositionGet",
    result = state.toResult(),
    errors = errors.toTransportErrors(),
    composition = compositionResponse.toTransport(),
    contextId = id.takeIf { it != IsContextId.NONE }?.asString()
)

fun IsContext.toTransport() =
    when (command) {
        IsCommand.ANALYSIS_GET -> toTransportAnalysisGet()
        IsCommand.ANALYSIS_CREATE -> toTransportAnalysisCreate()
        IsCommand.ANALYSIS_REGENERATE -> toTransportAnalysisRegenerate()
        IsCommand.COMPOSITION_CONTEXT_GET -> toTransportCompositionContextGet()
        IsCommand.COMPOSITION_CREATE_MANUAL -> toTransportCompositionCreateManual()
        IsCommand.COMPOSITION_CREATE_PHOTOS -> toTransportCompositionCreatePhotos()
        IsCommand.COMPOSITION_GET -> toTransportCompositionGet()
        IsCommand.DOWNLOAD_FILE -> toTransportDownloadFile()
        else -> throw UnknownIsCommand(command)
    }

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
)

fun IsCompositionContext.toTransport(): CompositionContext? = if (this.id == IsContextId.NONE) null else CompositionContext(
    id = id.asString(),
    state = state.toTransport(),
    errors = errors.toTransportErrors(),
    timeStart = timeStart.atOffset(ZoneOffset.UTC),
    composition = composition.toTransport()
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

fun IsColor.toTransport(): Color? = when (this) {
    IsColor.DARK_RED -> Color.DARK_RED
    IsColor.RED -> Color.RED
    IsColor.ORANGE -> Color.ORANGE
    IsColor.YELLOW -> Color.YELLOW
    IsColor.LIGHT_YELLOW -> Color.LIGHT_YELLOW
    IsColor.LIGHT_GREEN -> Color.LIGHT_GREEN
    IsColor.GREEN -> Color.GREEN
    IsColor.DARK_GREEN -> Color.DARK_GREEN
    IsColor.NONE -> null
}

fun IsRiskLevel.toTransport(): RiskLevel? = when (this) {
    IsRiskLevel.CRITICAL -> RiskLevel.CRITICAL
    IsRiskLevel.HIGH -> RiskLevel.HIGH
    IsRiskLevel.MEDIUM -> RiskLevel.MEDIUM
    IsRiskLevel.LOW -> RiskLevel.LOW
    IsRiskLevel.MINIMAL -> RiskLevel.MINIMAL
    IsRiskLevel.NONE -> null
}

fun IsState.toResult(): ResponseResult = when (this) {
    IsState.RUNNING, IsState.FINISHING -> ResponseResult.SUCCESS
    IsState.FAILING -> ResponseResult.ERROR
    IsState.NONE -> ResponseResult.ERROR
}

fun IsState.toTransport(): State? = when (this) {
    IsState.RUNNING -> State.RUNNING
    IsState.FINISHING -> State.FINISHING
    IsState.FAILING -> State.FAILING
    IsState.NONE -> State.NONE
}

// --- Error Mappers ---

fun List<IsError>.toTransportErrors(): List<Error>? = this
    .map { it.toTransport() }
    .toList()
    .takeIf { it.isNotEmpty() }

fun IsError.toTransport() = Error(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)