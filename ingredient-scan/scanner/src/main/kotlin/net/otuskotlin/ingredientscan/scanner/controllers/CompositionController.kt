package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.web.bind.annotation.RestController

@RestController
open class CompositionController(private val bizService: BizService): V1BaseController(), CompositionApi {
    override suspend fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): CompositionCreateByManualResponse {
        return bizService.execute(compositionCreateByManualRequest,"CompositionCreateByManual")
    }

    override suspend fun compositionGet(compositionGetRequest: CompositionGetRequest): CompositionGetResponse {
        return bizService.execute(compositionGetRequest, "CompositionGet")
    }

    override suspend fun compositionContextGet(compositionContextGetRequest: CompositionContextGetRequest): CompositionContextGetResponse {
        return bizService.execute(compositionContextGetRequest, "CompositionContextGet")
    }
}