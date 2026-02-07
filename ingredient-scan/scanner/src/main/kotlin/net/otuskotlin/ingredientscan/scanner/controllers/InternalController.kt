package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.internal.api.InternalApi
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizInternalService
import org.springframework.web.bind.annotation.RestController


@RestController
class InternalController(private val bizInternalService: BizInternalService): V1BaseController(), InternalApi {
    override suspend fun internalAnalysisFind(internalAnalysisFindRequest: InternalAnalysisFindRequest): InternalAnalysisFindResponse {
        return bizInternalService.execute(internalAnalysisFindRequest, "InternalAnalysisFind")
    }

    override suspend fun internalAnalysisSave(internalAnalysisSaveRequest: InternalAnalysisSaveRequest): InternalAnalysisSaveResponse {
        return bizInternalService.execute(internalAnalysisSaveRequest, "InternalAnalysisSave")
    }

    override suspend fun internalCompositionFind(internalCompositionFindRequest: InternalCompositionFindRequest): InternalCompositionFindResponse {
        return bizInternalService.execute(internalCompositionFindRequest, "InternalCompositionFind")
    }

    override suspend fun internalCompositionSave(internalCompositionSaveRequest: InternalCompositionSaveRequest): InternalCompositionSaveResponse {
        return bizInternalService.execute(internalCompositionSaveRequest, "InternalCompositionSave")
    }
}