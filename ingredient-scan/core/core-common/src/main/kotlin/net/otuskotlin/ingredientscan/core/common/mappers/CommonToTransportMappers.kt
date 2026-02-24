package net.otuskotlin.ingredientscan.core.common.mappers

import net.otuskotlin.ingredientscan.core.common.ai.AiComponent
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsRiskLevel

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
    composition = composition,
    regenerateId = analysis.id
)

fun IsContext.update() {
    if (id != context.id) {
        fail(
            errorContext(
                violationCode = "LIGHT_CONTEXT",
                message = "Incorrect light context: ${this.id} != ${context.id}"
            )
        )
        return
    }

    subCommand = context.subCommand
    state = context.state
    scan = context.scan
    analysis = context.analysis
    composition = context.composition

    for (er: IsError in context.errors) {
        if (!errors.contains(er)) {
            errors.add(er)
        }
    }
}

fun AiComponent.toTransport(): IsComponent = IsComponent(
    name = name,
    scientificName = scientific_name,
    description = description,
    sources = sources,
    riskLevel = IsRiskLevel.entries.find { it.name.equals(risk_level, ignoreCase = true) } ?: IsRiskLevel.NONE,
    healthRisks = health_risks
)
