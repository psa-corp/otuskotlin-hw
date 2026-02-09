package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanPhotosDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanType
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RequestMapping("/v1")
@RestController
class MediaController(private val bizService: BizService) {

    @PostMapping(value = ["/media/composition/create/photos"], consumes = ["multipart/form-data"])
    suspend fun compositionCreateByPhotos(@RequestPart("photos") photos: Flux<FilePart>): CompositionCreateByPhotosResponse {
        val scan = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(type = ScanType.PHOTO)
        )
        return bizService.execute(request = scan, photos = photos, operation = "CompositionCreateByPhotos")
    }
}