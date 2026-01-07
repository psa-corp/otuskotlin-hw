package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING


class IsBizProcessor(private val settings: IsCorSettings) {
    suspend fun exec(context: IsContext) {
        when (context.command){
            IsCommand.ANALYSIS_GET,
            IsCommand.ANALYSIS_REGENERATE -> context.analysisResponse = STUB_ANALYSIS
            IsCommand.COMPOSITION_GET -> context.compositionResponse = STUB_COMPOSITION
            IsCommand.COMPOSITION_CONTEXT_GET,
            IsCommand.COMPOSITION_CREATE_MANUAL,
            IsCommand.COMPOSITION_CREATE_PHOTOS -> {
                context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
                context.id = STUB_COMPOSITION_CONTEXT_FINISHING.id
            }
            else -> {}
        }

        context.state = IsState.FINISHING
        settings.contextRepository?.save(context)
    }
}