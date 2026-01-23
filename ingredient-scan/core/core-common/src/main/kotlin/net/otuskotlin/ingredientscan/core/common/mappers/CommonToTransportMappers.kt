package net.otuskotlin.ingredientscan.core.common.mappers

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext

fun IsContext.toCompositionContext() = IsCompositionContext(
    id = id,
    state = state,
    errors = errors,
    timeStart = timeStart,
    composition = compositionResponse
)