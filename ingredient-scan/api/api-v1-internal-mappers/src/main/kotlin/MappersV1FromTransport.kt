package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownRequestClass


// --- Analysis Mappers ---

fun InternalContext.fromTransport(request: InternalAnalysisFindRequest) {
    command = InternalCommand.ANALYSIS_FIND
    compositionIdRequest = request.compositionId.toCompositionId()
}

fun InternalContext.fromTransport(request: InternalAnalysisSaveRequest) {
    command = InternalCommand.ANALYSIS_SAVE
    analysisRequest = request.analysis.toInternal()
}

fun InternalContext.fromTransport(request: InternalCompositionFindRequest) {
    command = InternalCommand.COMPOSITION_FIND
    compositionTextRequest = request.text
}

fun InternalContext.fromTransport(request: InternalCompositionSaveRequest) {
    command = InternalCommand.COMPOSITION_SAVE
    compositionRequest = request.composition.toInternal()
}

fun InternalContext.fromTransport(request: InternalRequest) = when (request) {
    is InternalAnalysisFindRequest -> fromTransport(request)
    is InternalAnalysisSaveRequest -> fromTransport(request)
    is InternalCompositionFindRequest -> fromTransport(request)
    is InternalCompositionSaveRequest -> fromTransport(request)
    else -> throw UnknownRequestClass(request.javaClass)
}


