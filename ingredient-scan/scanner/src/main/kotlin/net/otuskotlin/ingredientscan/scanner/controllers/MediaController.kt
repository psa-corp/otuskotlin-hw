package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.MediaApi
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
open class MediaController(private val bizService: BizService) : MediaApi {

    override suspend fun compositionCreateByPhotos(
        photos: Array<MultipartFile>,
        scan: CompositionCreateByPhotosRequest
    ): ResponseEntity<CompositionCreateByPhotosResponse> {
        val resp = bizService.execute(scan, photos) as CompositionCreateByPhotosResponse
        if(resp.errors.isNullOrEmpty()) {
            ResponseEntity.ok(resp)
        }
        return ResponseEntity.badRequest().body(resp)
    }
}