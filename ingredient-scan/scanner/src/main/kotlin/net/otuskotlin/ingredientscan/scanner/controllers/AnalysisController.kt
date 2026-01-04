package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.AnalysisApi
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportAnalysisGet
import net.otuskotlin.ingredientscan.mappers.v1.toTransportAnalysisRegenerate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
open class AnalysisController: AnalysisApi {
    override suspend fun analysisGet(analysisGetRequest: AnalysisGetRequest): AnalysisGetResponse {
        val context = IsContext()
        context.fromTransport(analysisGetRequest)
        context.analysisResponse = STUB_ANALYSIS
        context.state = IsState.FINISHING
        return context.toTransportAnalysisGet()
    }

    override suspend fun analysisRegenerate(analysisRegenerateRequest: AnalysisRegenerateRequest): AnalysisRegenerateResponse {
        val context = IsContext()
        context.fromTransport(analysisRegenerateRequest)
        context.analysisResponse = STUB_ANALYSIS
        context.state = IsState.FINISHING
        return context.toTransportAnalysisRegenerate()
    }
}