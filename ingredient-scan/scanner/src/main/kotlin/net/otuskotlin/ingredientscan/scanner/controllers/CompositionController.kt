package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionContextGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionContextGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionContextGet
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreateManual
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreatePhotos
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionGet
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
open class CompositionController(private val s3CloudService: S3CloudService,
                                 private val bizService: BizService) : CompositionApi {
    private val log = LoggerFactory.getLogger(CompositionController::class.java)

    override suspend fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): CompositionCreateByManualResponse {
        val context = IsContext()
        context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
        context.state = IsState.FINISHING
        return context.toTransportCompositionCreateManual()
    }

    override suspend fun compositionCreateByPhotos(
        photos: Array<MultipartFile>,
        scan: CompositionCreateByPhotosRequest
    ): CompositionCreateByPhotosResponse {

        val context = IsContext()
        val photoUrls = s3CloudService.uploadFiles(context, photos, null)
        context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
        context.state = IsState.FINISHING
        return context.toTransportCompositionCreatePhotos()
    }

    override suspend fun compositionGet(compositionGetRequest: CompositionGetRequest): CompositionGetResponse {
        val context = IsContext()
        context.compositionResponse = STUB_COMPOSITION
        context.state = IsState.FINISHING
        return context.toTransportCompositionGet()
    }

    override suspend fun compositionContextGet(compositionContextGetRequest: CompositionContextGetRequest): CompositionContextGetResponse {
        val context = IsContext()
        context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
        context.state = IsState.FINISHING
        return context.toTransportCompositionContextGet()
    }
}