package net.otuskotlin.ingredientscan.core.common.mappers

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsScan

fun IsContext.toCompositionContext() = IsCompositionContext(
    id = id,
    state = state,
    errors = errors,
    timeStart = timeStart,
    composition = compositionResponse
)

fun IsContext.toLightContext() = IsLightContext(
    id = id,
    command = command,
    subCommand = subCommand,
    timeStart = timeStart,
    state = state,
    errors = errors,
    requestId = requestId,
    scan = scan,
    analysis = analysis,
    composition = composition
)