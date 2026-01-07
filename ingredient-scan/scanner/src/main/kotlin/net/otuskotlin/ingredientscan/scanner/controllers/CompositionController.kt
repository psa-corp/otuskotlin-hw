package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.web.bind.annotation.RestController

@RestController
open class CompositionController(private val bizService: BizService) : CompositionApi {
    override suspend fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): CompositionCreateByManualResponse {
        return bizService.execute(compositionCreateByManualRequest) as CompositionCreateByManualResponse
    }

    override suspend fun compositionGet(compositionGetRequest: CompositionGetRequest): CompositionGetResponse {
        return bizService.execute(compositionGetRequest) as CompositionGetResponse
    }

    override suspend fun compositionContextGet(compositionContextGetRequest: CompositionContextGetRequest): CompositionContextGetResponse {
        return bizService.execute(compositionContextGetRequest) as CompositionContextGetResponse
    }
}