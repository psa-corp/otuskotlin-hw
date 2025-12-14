package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.DownloadApi
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.mappers.v1.toDownloadFileErrorResponse
import net.otuskotlin.ingredientscan.scanner.services.s3.JsonErrorResource
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets


@RestController
open class DownloadController(private val s3CloudService: S3CloudService) : DownloadApi  {

    override fun downloadFile(fileName: String): ResponseEntity<Resource> {
        val cleanedFileName = fileName.removePrefix("/")
        val context = IsContext()
        val metadata = s3CloudService.getObjectMetadata(context, cleanedFileName)
        val resource = s3CloudService.downloadFileAsResource(context, cleanedFileName)

        if (context.errors.isEmpty() && metadata != null && resource != null) {
            val fileNameForHeader = cleanedFileName.substringAfterLast("/")

            val contentDisposition = ContentDisposition.inline()
                .filename(fileNameForHeader, StandardCharsets.UTF_8)
                .build()

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, metadata.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource)
        }

        if (context.errors.isEmpty()) {
            context.errors.add(IsError(code = "FILE_NOT_FOUND", group = "s3", field = "", message = "File not found: $cleanedFileName"))
        }

        val errorResponse = context.toDownloadFileErrorResponse()
        val jsonResource = JsonErrorResource(errorResponse)

        val status = when (context.errors.firstOrNull()?.code) {
            "FILE_NOT_FOUND" -> HttpStatus.NOT_FOUND
            "STORE_NOT_FOUND" -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonResource)
    }
}