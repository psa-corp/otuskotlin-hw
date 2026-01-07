package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.AnalysisApi
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateResponse
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.web.bind.annotation.RestController

@RestController
open class AnalysisController(private val bizService: BizService): V1BaseController(), AnalysisApi {
    override suspend fun analysisGet(analysisGetRequest: AnalysisGetRequest): AnalysisGetResponse {
         return bizService.execute(analysisGetRequest) as AnalysisGetResponse
    }

    override suspend fun analysisRegenerate(analysisRegenerateRequest: AnalysisRegenerateRequest): AnalysisRegenerateResponse {
        return bizService.execute(analysisRegenerateRequest) as AnalysisRegenerateResponse
    }
}