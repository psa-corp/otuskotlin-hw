package net.otuskotlin.ingredientscan.mappers.v1.internal

import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.mappers.v1.internal.exceptions.UnknownRequestClass


// --- Analysis Mappers ---

fun InternalContext.fromTransportInternal(request: InternalAnalysisFindRequest) {
    command = InternalCommand.ANALYSIS_FIND
    compositionIdRequest = request.compositionId.toCompositionId()
}

fun InternalContext.fromTransportInternal(request: InternalAnalysisSaveRequest) {
    command = InternalCommand.ANALYSIS_SAVE
    analysisRequest = request.analysis.toInternal()
}

fun InternalContext.fromTransportInternal(request: InternalCompositionFindRequest) {
    command = InternalCommand.COMPOSITION_FIND
    compositionTextRequest = request.text
}

fun InternalContext.fromTransportInternal(request: InternalCompositionSaveRequest) {
    command = InternalCommand.COMPOSITION_SAVE
    compositionRequest = request.composition.toInternal()
}

fun InternalContext.fromTransportInternal(request: InternalRequest) = when (request) {
    is InternalAnalysisFindRequest -> fromTransportInternal(request)
    is InternalAnalysisSaveRequest -> fromTransportInternal(request)
    is InternalCompositionFindRequest -> fromTransportInternal(request)
    is InternalCompositionSaveRequest -> fromTransportInternal(request)
    else -> throw UnknownRequestClass(request.javaClass)
}


