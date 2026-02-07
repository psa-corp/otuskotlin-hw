package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysis
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisFindResponse
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisSaveResponse

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalColor
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
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownIsCommand
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
)

fun InternalComposition.toInternal(): IsComposition = IsComposition(
    id = id.toCompositionId(),
    text = text,
    createDate = createDate.toLocalDateTime(),
)

fun InternalColor.toInternal(): IsColor = when (this) {
    InternalColor.DARK_RED -> IsColor.DARK_RED
    InternalColor.RED -> IsColor.RED
    InternalColor.ORANGE -> IsColor.ORANGE
    InternalColor.YELLOW -> IsColor.YELLOW
    InternalColor.LIGHT_YELLOW -> IsColor.LIGHT_YELLOW
    InternalColor.LIGHT_GREEN -> IsColor.LIGHT_GREEN
    InternalColor.GREEN -> IsColor.GREEN
    InternalColor.DARK_GREEN -> IsColor.DARK_GREEN
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
    IsColor.DARK_RED -> InternalColor.DARK_RED
    IsColor.RED -> InternalColor.RED
    IsColor.ORANGE -> InternalColor.ORANGE
    IsColor.YELLOW -> InternalColor.YELLOW
    IsColor.LIGHT_YELLOW -> InternalColor.LIGHT_YELLOW
    IsColor.LIGHT_GREEN -> InternalColor.LIGHT_GREEN
    IsColor.GREEN -> InternalColor.GREEN
    IsColor.DARK_GREEN -> InternalColor.DARK_GREEN
    IsColor.NONE -> InternalColor.NONE
}

fun IsAnalysis.toInternalTransport(): InternalAnalysis? = if (this.id == IsAnalysisId.NONE) null else InternalAnalysis(
    id = id.asString(),
    compositionId = compositionId.asString(),
    createDate = createDate.atOffset(ZoneOffset.UTC),
    description = description,
    rating = rating,
    color = color.toInternalTransport() ?: InternalColor.NONE,
    problematicComponent = mutableListOf(),
    safeComponent = mutableListOf()
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

fun InternalContext.toTransport() =
    when (command) {
        InternalCommand.ANALYSIS_FIND -> toTransportInternalAnalysisFind()
        InternalCommand.ANALYSIS_SAVE -> toTransportInternalAnalysisSave()
        InternalCommand.COMPOSITION_FIND -> toTransportInternalCompositionFind()
        InternalCommand.COMPOSITION_SAVE -> toTransportInternalCompositionSave()
        else -> throw UnknownIsCommand(command)
    }
