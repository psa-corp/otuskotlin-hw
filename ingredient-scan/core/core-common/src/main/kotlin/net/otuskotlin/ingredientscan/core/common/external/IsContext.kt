package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponentFilter
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsRequestId
import net.otuskotlin.ingredientscan.core.common.external.models.IsScan
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsWorkMode
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import java.time.LocalDateTime
import java.util.UUID.randomUUID

data class IsContext(
    var id: IsContextId = IsContextId("context-${randomUUID()}"),
    var command: IsCommand = IsCommand.NONE,
    var subCommand: IsSubCommand = IsSubCommand.NONE,
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
    var compositionIdRequest: IsCompositionId = IsCompositionId.NONE,
    var contextIdRequest: IsContextId = IsContextId.NONE,
    var analysisIdRequest: IsAnalysisId = IsAnalysisId.NONE,

    // Response data
    var analysisResponse: IsAnalysis = IsAnalysis(),
    var compositionResponse: IsComposition = IsComposition(),
    var componentResponse: IsComponent = IsComponent(),
    var componentsResponse: MutableList<IsComponent> = mutableListOf(),
    var scanResponse: IsScan = IsScan(),
    var compositionContextResponse: IsCompositionContext = IsCompositionContext(),

    // Settings
    var settings: IsCorSettings = IsCorSettings.NONE,

    // Validate
    var validateCompositionId: IsCompositionId = IsCompositionId.NONE,
    var validateContextId: IsContextId = IsContextId.NONE,
    var validateAnalysisId: IsAnalysisId = IsAnalysisId.NONE,
    var validateScan: IsScan = IsScan.NONE,


    // Validated
    var validatedCompositionId: IsCompositionId = IsCompositionId.NONE,
    var validatedContextId: IsContextId = IsContextId.NONE,
    var validatedAnalysisId: IsAnalysisId = IsAnalysisId.NONE,
    var validatedScan: IsScan = IsScan.NONE,

    // Repo
    var compositionRepo: IsCompositionRepository? = null,
    var contextRepo: IsContextRepository? = null,
    var analysisRepo: IsAnalysisRepository? = null,

    )