@file:Suppress("UNCHECKED_CAST")

package net.otuskotlin.ingredientscan.app.content

import net.otuskotlin.ingredientscan.api.log1.mapper.toLog
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.mappers.v1.toDownloadFileErrorResponse
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import kotlin.reflect.KClass

suspend inline fun IsAppSettings.downloadHelper(
    fileName: String,
    clazz: KClass<*>,
    logId: String,
): ResponseEntity<Resource> {
    val logger = settings.loggerProvider.logger(clazz)
    val context = IsContext(
        timeStart = LocalDateTime.now(),
    )
    try {
        val contentProvider = settings.contentProvider
        if (contentProvider == null) {
            context.errors.add(
                IsError(
                    code = "SERVICE_UNAVAILABLE",
                    group = "s3",
                    field = "",
                    message = "Content provider is not configured"
                ))

            throw IllegalStateException("Content provider is not configured")
        }
        logger.info(
            msg = "Request $logId started for ${clazz.simpleName}",
            marker = "BIZ",
            data = "file name:$fileName"
        )

        return contentProvider.download(context, fileName) as ResponseEntity<Resource>

    } catch (e: Throwable) {
        val cleanedFileName = fileName.removePrefix("/")

        if (context.errors.isEmpty()) {
            context.errors.add(
                IsError(
                    code = "FILE_NOT_FOUND",
                    group = "s3",
                    field = "",
                    message = "File not found: $cleanedFileName"
                )
            )
        }

        val errorResponse = context.toDownloadFileErrorResponse()
        val jsonResource = JsonErrorResource(errorResponse)

        val status = when (context.errors.firstOrNull()?.code) {
            "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE
            "FILE_NOT_FOUND" -> HttpStatus.NOT_FOUND
            "STORE_NOT_FOUND" -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        logger.info(
            msg = "Response error $logId started for ${clazz.simpleName}",
            marker = "BIZ",
            data = errorResponse
        )

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonResource)
    }
}
