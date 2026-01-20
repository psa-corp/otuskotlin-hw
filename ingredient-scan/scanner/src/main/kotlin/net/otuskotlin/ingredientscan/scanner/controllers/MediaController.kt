package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@Validated
open class MediaController(private val bizService: BizService): V1BaseController() {

    @PostMapping(value = ["/media/composition/create/photos"], produces = ["application/json"], consumes = ["multipart/form-data"])
     suspend fun compositionCreateByPhotos(
        @RequestPart("photos") photos: Flux<FilePart>,
        @RequestPart("scan") scan: CompositionCreateByPhotosRequest
    ): CompositionCreateByPhotosResponse {
        return bizService.execute(scan, photos, "CompositionCreateByPhotos")
    }
}