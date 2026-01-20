package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.app.common.submitHelper
import net.otuskotlin.ingredientscan.app.content.uploadHelper
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.mappers.v1.toDownloadFileErrorResponse
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import net.otuskotlin.ingredientscan.scanner.services.s3.JsonErrorResource
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets

@Service
open class BizService(
    private val kafkaSender: BizKafkaSender,
    private val compositionRepository: InMemoryCompositionRepository,
    private val contextRepository: InMemoryContextRepository,
    private val s3CloudService: S3CloudService,
) {
    private val appSettings: IsAppSettings
    private val log = LoggerFactory.getLogger(BizService::class.java)

    init {
        val settings = IsCorSettings(
            messageSender = kafkaSender,
            contextRepository = contextRepository,
            contentProvider = s3CloudService
        )

        appSettings = AppSettings(
            settings = settings,
            processor = IsBizProcessor(settings),
            subProcessor = IsBizSubProcessor(settings)
        )
        log.info("BizService initialized")
    }

    open suspend fun <R : IResponse> execute(request: IRequest, operation : String) : R {
        return appSettings.submitHelper(request, this::class, operation)
    }

    open suspend fun <R : IResponse> execute(request: IRequest, photos: Flux<FilePart>, operation : String) : R {
        return appSettings.uploadHelper(request, photos,this::class, operation)
    }

    open suspend fun get(fileName: String) : ResponseEntity<Resource> {
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