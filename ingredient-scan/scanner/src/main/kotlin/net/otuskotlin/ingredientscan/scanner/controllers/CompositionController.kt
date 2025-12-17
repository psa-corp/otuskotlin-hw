package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreateManual
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreatePhotos
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionGet
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
open class CompositionController(private val s3CloudService: S3CloudService,
                                 private val bizService: BizService) : CompositionApi {
    private val log = LoggerFactory.getLogger(CompositionController::class.java)

    override fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): ResponseEntity<CompositionCreateByManualResponse> {
        val context = IsContext()
        return try {
            context.fromTransport(compositionCreateByManualRequest)

            log.info(
                "Context created:\n" +
                        "  requestId: {}\n" +
                        "  command: {}\n" +
                        "  compositionText: {}",
                context.requestId.asString(),
                context.command,
                context.compositionRequest.text.take(50)
            )

            bizService.compositionCreateByManual(context)
            // Финальный результат придёт асинхронно через WebSocket/polling
            ResponseEntity.ok(context.toTransportCompositionCreateManual())

        } catch (e: Exception) {
            log.error("Error in compositionCreateByManual", e)
            context.command = IsCommand.COMPOSITION_CREATE_MANUAL
            context.compositionResponse.id = IsCompositionId.NONE
            context.state = IsState.FAILING
            context.errors.add(
                IsError(
                    code = "CONTROLLER_ERROR",
                    group = "COMPOSITION_CREATE_MANUAL",
                    field = "controller",
                    message = "Error processing request: ${e.message}"
                )
            )

            ResponseEntity.badRequest().body(context.toTransportCompositionCreateManual())
        }
    }

    override fun compositionCreateByPhotos(
        photos: Array<MultipartFile>,
        scan: CompositionCreateByPhotosRequest
    ): ResponseEntity<CompositionCreateByPhotosResponse> {
        val context = IsContext()
        return try {
            //Загружаем фото в S3 облако
            val photoUrls = s3CloudService.uploadFiles(context, photos, null)

            if (context.errors.isNotEmpty()) {
                log.error("Error. Photos can't uploaded to S3:{}", context.errors)
                context.state = IsState.FAILING
                return ResponseEntity.badRequest().body(context.toTransportCompositionCreatePhotos())
            }

            log.info("Photos uploaded to S3: {}", photoUrls.size)
            context.fromTransport(scan, photoUrls)

            bizService.compositionCreateByPhotos(context)
            // Финальный результат придёт асинхронно через WebSocket/polling
            ResponseEntity.ok(context.toTransportCompositionCreatePhotos())

        } catch (e: Exception) {
            log.error("Error in compositionCreateByPhotos", e)

            context.command = IsCommand.COMPOSITION_CREATE_PHOTOS
            context.compositionResponse.id = IsCompositionId.NONE
            context.state = IsState.FAILING
            context.errors.add(
                    IsError(
                        code = "CONTROLLER_ERROR",
                        group = "COMPOSITION_CREATE_PHOTOS",
                        field = "controller",
                        message = "Error processing request: ${e.message}"
                    )
                )

            ResponseEntity.badRequest().body(context.toTransportCompositionCreatePhotos())
        }
    }

    override fun compositionGet(compositionGetRequest: CompositionGetRequest): ResponseEntity<CompositionGetResponse> {
        val context = IsContext()
        context.fromTransport(compositionGetRequest)
        bizService.compositionGet(context)
        return ResponseEntity.ok(context.toTransportCompositionGet())
    }
}