package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.DownloadApi
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController


@RestController
open class DownloadController : DownloadApi  {
    private val foodJpeg = ClassPathResource("static/food.jpg")

    override fun downloadFile(fileName: String): ResponseEntity<Resource> {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"food.jpg\"")
            .contentType(MediaType.IMAGE_JPEG)
            .body(foodJpeg)
    }
}