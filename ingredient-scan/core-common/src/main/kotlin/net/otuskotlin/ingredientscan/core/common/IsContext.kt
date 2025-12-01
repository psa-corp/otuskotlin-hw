package net.otuskotlin.ingredientscan.core.common

import net.otuskotlin.ingredientscan.core.common.models.*
import net.otuskotlin.ingredientscan.core.common.stubs.IsStubs
import java.time.LocalDateTime

data class IsContext(
    var command: IsCommand = IsCommand.NONE,
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),

    var workMode: IsWorkMode = IsWorkMode.PROD,
    var stubCase: IsStubs = IsStubs.NONE,

    var requestId: IsRequestId = IsRequestId.NONE,
    var timeStart: LocalDateTime = LOCAL_DATE_TIME_NONE,

    // Request data
    var analysisRequest: IsAnalysis = IsAnalysis(),
    var compositionRequest: IsComposition = IsComposition(),
    var componentRequest: IsComponent = IsComponent(),
    var scanRequest: IsScan = IsScan(),
    var filterRequest: IsComponentFilter = IsComponentFilter(),

    // Response data
    var analysisResponse: IsAnalysis = IsAnalysis(),
    var compositionResponse: IsComposition = IsComposition(),
    var componentResponse: IsComponent = IsComponent(),
    var componentsResponse: MutableList<IsComponent> = mutableListOf(),
    var scanResponse: IsScan = IsScan(),
)