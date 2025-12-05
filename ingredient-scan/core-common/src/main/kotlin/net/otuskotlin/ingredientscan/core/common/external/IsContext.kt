package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponentFilter
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsRequestId
import net.otuskotlin.ingredientscan.core.common.external.models.IsScan
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsWorkMode
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsStubs
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