package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreateManual
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreatePhotos
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionGet
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
open class CompositionController(private val s3CloudService: S3CloudService) : CompositionApi {
    private val log = LoggerFactory.getLogger(CompositionController::class.java)

    override fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): ResponseEntity<CompositionCreateByManualResponse> {
        val context = IsContext()
        context.fromTransport(compositionCreateByManualRequest)
        context.compositionResponse = STUB_COMPOSITION
        context.state = IsState.FINISHING
        return ResponseEntity.ok(context.toTransportCompositionCreateManual())
    }

    override fun compositionCreateByPhotos(
        photos: Array<MultipartFile>,
        scan: CompositionCreateByPhotosRequest
    ): ResponseEntity<CompositionCreateByPhotosResponse> {
        val context = IsContext()
        log.info("!!!!!!!!!!! photos:{}, photos size:{}", photos, photos.size)

        var list = s3CloudService.uploadFiles(context, photos,  null)
        log.info("!!!!!!!!!!! errors:{}, errors size:{}", context.errors, context.errors.size)
        if (context.errors.isNotEmpty()) {
            context.state = IsState.FAILING
            return ResponseEntity.badRequest().body(context.toTransportCompositionCreatePhotos())
        }

        context.fromTransport(scan, list)
        context.compositionResponse = STUB_COMPOSITION
        context.state = IsState.FINISHING
        return ResponseEntity.ok(context.toTransportCompositionCreatePhotos())
    }

    override fun compositionGet(compositionGetRequest: CompositionGetRequest): ResponseEntity<CompositionGetResponse> {
        val context = IsContext()
        context.fromTransport(compositionGetRequest)
        context.compositionResponse = STUB_COMPOSITION
        context.state = IsState.FINISHING
        return ResponseEntity.ok(context.toTransportCompositionGet())
    }
}