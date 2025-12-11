package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.CompositionApi
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreateManual
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionCreatePhotos
import net.otuskotlin.ingredientscan.mappers.v1.toTransportCompositionGet
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
open class CompositionController : CompositionApi {
    override fun compositionCreateByManual(compositionCreateByManualRequest: CompositionCreateByManualRequest): ResponseEntity<CompositionCreateByManualResponse> {
        val context = IsContext()
        context.fromTransport(compositionCreateByManualRequest)


        return ResponseEntity.ok(context.toTransportCompositionCreateManual())
    }

    override fun compositionCreateByPhotos(
        photos: Array<MultipartFile>,
        scan: CompositionCreateByPhotosRequest
    ): ResponseEntity<CompositionCreateByPhotosResponse> {
        val context = IsContext()
        var list = upload(photos)

        context.fromTransport(scan, list)

        return ResponseEntity.ok(context.toTransportCompositionCreatePhotos())
    }

    override fun compositionGet(compositionGetRequest: CompositionGetRequest): ResponseEntity<CompositionGetResponse> {
        val context = IsContext()
        context.fromTransport(compositionGetRequest)
        return ResponseEntity.ok(context.toTransportCompositionGet())
    }

    private fun upload(photos: Array<MultipartFile>): MutableList<String> = mutableListOf<String>().apply {}

}