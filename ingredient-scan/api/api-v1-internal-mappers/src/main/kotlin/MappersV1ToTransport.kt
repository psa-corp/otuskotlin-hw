package net.otuskotlin.ingredientscan.mappers.v1.internal

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysis
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisFindResponse
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisSaveResponse

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalColor
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalComponent
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalComposition
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionFindResponse
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionSaveResponse
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponseResult

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalError
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRiskLevel
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import net.otuskotlin.ingredientscan.core.common.external.models.IsRiskLevel
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.internal.exceptions.UnknownIsCommand
import java.time.ZoneOffset

fun String?.toCompositionId() = this?.let { IsCompositionId(it) } ?: IsCompositionId.NONE
fun String?.toAnalysisId() = this?.let { IsAnalysisId(it) } ?: IsAnalysisId.NONE

fun InternalAnalysis.toInternal(): IsAnalysis = IsAnalysis(
    id = id.toAnalysisId(),
    compositionId = compositionId.toCompositionId(),
    createDate =  createDate.toLocalDateTime(),
    description = description,
    rating = rating,
    color = color.toInternal(),
    components = components?.map { it.toTransport() }?.toMutableList() ?: mutableListOf()
)

fun InternalComponent.toTransport(): IsComponent = IsComponent(
    name = name?: "",
    scientificName = scientificName?: "",
    description = description?: "",
    sources = sources?: "",
    riskLevel = riskLevel?.toTransport() ?: IsRiskLevel.NONE,
    healthRisks = healthRisks?: ""
)

fun InternalComposition.toInternal(): IsComposition = IsComposition(
    id = id.toCompositionId(),
    text = text,
    createDate = createDate.toLocalDateTime(),
)

fun InternalColor.toInternal(): IsColor = when (this) {
    InternalColor.VERY_DARK_RED -> IsColor.VERY_DARK_RED
    InternalColor.DARK_RED -> IsColor.DARK_RED
    InternalColor.DEEP_RED -> IsColor.DEEP_RED
    InternalColor.RED -> IsColor.RED
    InternalColor.LIGHT_RED -> IsColor.LIGHT_RED
    InternalColor.RED_ORANGE -> IsColor.RED_ORANGE
    InternalColor.ORANGE -> IsColor.ORANGE
    InternalColor.LIGHT_ORANGE -> IsColor.LIGHT_ORANGE
    InternalColor.DARK_YELLOW -> IsColor.DARK_YELLOW
    InternalColor.YELLOW -> IsColor.YELLOW
    InternalColor.LIGHT_YELLOW -> IsColor.LIGHT_YELLOW
    InternalColor.YELLOW_GREEN -> IsColor.YELLOW_GREEN
    InternalColor.PALE_GREEN -> IsColor.PALE_GREEN
    InternalColor.LIGHT_GREEN -> IsColor.LIGHT_GREEN
    InternalColor.GREEN -> IsColor.GREEN
    InternalColor.MEDIUM_GREEN -> IsColor.MEDIUM_GREEN
    InternalColor.BRIGHT_GREEN -> IsColor.BRIGHT_GREEN
    InternalColor.VIBRANT_GREEN -> IsColor.VIBRANT_GREEN
    InternalColor.FRESH_GREEN -> IsColor.FRESH_GREEN
    InternalColor.BRILLIANT_GREEN -> IsColor.BRILLIANT_GREEN
    InternalColor.NONE -> IsColor.NONE
}

fun IsState.toResult(): InternalResponseResult = when (this) {
    IsState.RUNNING, IsState.FINISHING -> InternalResponseResult.SUCCESS
    IsState.FAILING -> InternalResponseResult.ERROR
    IsState.NONE -> InternalResponseResult.ERROR
}

fun List<IsError>.toInternalTransportErrors(): List<InternalError>? = this
    .map { it.toInternalTransport() }
    .toList()
    .takeIf { it.isNotEmpty() }

fun IsError.toInternalTransport() = InternalError(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)

fun IsColor.toInternalTransport(): InternalColor? = when (this) {
    IsColor.VERY_DARK_RED -> InternalColor.VERY_DARK_RED
    IsColor.DARK_RED -> InternalColor.DARK_RED
    IsColor.DEEP_RED -> InternalColor.DEEP_RED
    IsColor.RED -> InternalColor.RED
    IsColor.LIGHT_RED -> InternalColor.LIGHT_RED
    IsColor.RED_ORANGE -> InternalColor.RED_ORANGE
    IsColor.ORANGE -> InternalColor.ORANGE
    IsColor.LIGHT_ORANGE -> InternalColor.LIGHT_ORANGE
    IsColor.DARK_YELLOW -> InternalColor.DARK_YELLOW
    IsColor.YELLOW -> InternalColor.YELLOW
    IsColor.LIGHT_YELLOW -> InternalColor.LIGHT_YELLOW
    IsColor.YELLOW_GREEN -> InternalColor.YELLOW_GREEN
    IsColor.PALE_GREEN -> InternalColor.PALE_GREEN
    IsColor.LIGHT_GREEN -> InternalColor.LIGHT_GREEN
    IsColor.GREEN -> InternalColor.GREEN
    IsColor.MEDIUM_GREEN -> InternalColor.MEDIUM_GREEN
    IsColor.BRIGHT_GREEN -> InternalColor.BRIGHT_GREEN
    IsColor.VIBRANT_GREEN -> InternalColor.VIBRANT_GREEN
    IsColor.FRESH_GREEN -> InternalColor.FRESH_GREEN
    IsColor.BRILLIANT_GREEN -> InternalColor.BRILLIANT_GREEN
    IsColor.NONE -> InternalColor.NONE
}

fun IsAnalysis.toInternalTransport(): InternalAnalysis? = if (this.id == IsAnalysisId.NONE) null else InternalAnalysis(
    id = id.asString(),
    compositionId = compositionId.asString(),
    createDate = createDate.atOffset(ZoneOffset.UTC),
    description = description,
    rating = rating,
    color = color.toInternalTransport() ?: InternalColor.NONE,
    components = components.map { it.toInternalTransport() }.toMutableList()
)

fun IsComponent.toInternalTransport(): InternalComponent = InternalComponent(
    name = name.takeIf { it.isNotBlank() },
    scientificName = scientificName.takeIf { it.isNotBlank() },
    description = description.takeIf { it.isNotBlank() },
    sources = sources.takeIf { it.isNotBlank() },
    riskLevel = riskLevel.toTransport(),
    healthRisks = healthRisks.takeIf { it.isNotBlank() }
)

fun IsComposition.toInternalTransport(): InternalComposition? = if (this.id == IsCompositionId.NONE) null else InternalComposition(
    id = id.asString(),
    createDate = createDate.atOffset(ZoneOffset.UTC),
    text = text,
)

fun InternalContext.toTransportInternalAnalysisFind() = InternalAnalysisFindResponse(
    responseType = "internalAnalysisFind",
    result = state.toResult(),
    errors = errors.toInternalTransportErrors(),
    analysis = analysisResponse.toInternalTransport()
)

fun InternalContext.toTransportInternalAnalysisSave() = InternalAnalysisSaveResponse(
    responseType = "internalAnalysisSave",
    result = state.toResult(),
    errors = errors.toInternalTransportErrors(),
    analysis = analysisResponse.toInternalTransport()
)

fun InternalContext.toTransportInternalCompositionFind() = InternalCompositionFindResponse(
    responseType = "internalCompositionFind",
    result = state.toResult(),
    errors = errors.toInternalTransportErrors(),
    composition = compositionResponse.toInternalTransport()
)

fun InternalContext.toTransportInternalCompositionSave() = InternalCompositionSaveResponse(
    responseType = "internalCompositionSave",
    result = state.toResult(),
    errors = errors.toInternalTransportErrors(),
    composition = compositionResponse.toInternalTransport()
)

fun InternalContext.toTransportInternal() =
    when (command) {
        InternalCommand.ANALYSIS_FIND -> toTransportInternalAnalysisFind()
        InternalCommand.ANALYSIS_SAVE -> toTransportInternalAnalysisSave()
        InternalCommand.COMPOSITION_FIND -> toTransportInternalCompositionFind()
        InternalCommand.COMPOSITION_SAVE -> toTransportInternalCompositionSave()
        else -> throw UnknownIsCommand(command)
    }

fun IsRiskLevel.toTransport(): InternalRiskLevel? = when (this) {
    IsRiskLevel.CRITICAL -> InternalRiskLevel.CRITICAL
    IsRiskLevel.HIGH -> InternalRiskLevel.HIGH
    IsRiskLevel.MEDIUM -> InternalRiskLevel.MEDIUM
    IsRiskLevel.LOW -> InternalRiskLevel.LOW
    IsRiskLevel.MINIMAL -> InternalRiskLevel.MINIMAL
    IsRiskLevel.NONE -> null
}

fun InternalRiskLevel.toTransport(): IsRiskLevel = when (this) {
    InternalRiskLevel.CRITICAL -> IsRiskLevel.CRITICAL
    InternalRiskLevel.HIGH -> IsRiskLevel.HIGH
    InternalRiskLevel.MEDIUM -> IsRiskLevel.MEDIUM
    InternalRiskLevel.LOW -> IsRiskLevel.LOW
    InternalRiskLevel.MINIMAL -> IsRiskLevel.MINIMAL
}