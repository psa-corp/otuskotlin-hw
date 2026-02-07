package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.*
import java.time.LocalDateTime
import java.util.UUID.randomUUID

data class InternalContext(
    var id: IsContextId = IsContextId("context-${randomUUID()}"),
    var command: InternalCommand = InternalCommand.NONE,
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),

    var requestId: IsRequestId = IsRequestId.NONE,
    var timeStart: LocalDateTime = LOCAL_DATE_TIME_NONE,

    // Request data
    var analysisRequest: IsAnalysis = IsAnalysis.NONE,
    var compositionRequest: IsComposition = IsComposition.NONE,
    var compositionIdRequest: IsCompositionId = IsCompositionId.NONE,
    var compositionTextRequest: String = "",

    // Response data
    var analysisResponse: IsAnalysis = IsAnalysis.NONE,
    var compositionResponse: IsComposition = IsComposition.NONE,
)