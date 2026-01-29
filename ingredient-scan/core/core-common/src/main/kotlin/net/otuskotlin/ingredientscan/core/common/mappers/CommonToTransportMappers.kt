package net.otuskotlin.ingredientscan.core.common.mappers

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext

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
    requestId = requestId
)