@file:Suppress("UNCHECKED_CAST")

package net.otuskotlin.ingredientscan.app.content

import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorCustom
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import kotlin.reflect.KClass

suspend inline fun IsAppSettings.downloadHelper(
    fileNames: List<String>,
    clazz: KClass<*>,
    logId: String,
): Flux<DataBuffer> {
    val logger = settings.loggerProvider.logger(clazz)
    val context = IsContext(
        timeStart = LocalDateTime.now(),
    )

    val contextRepository = settings.contextRepository
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
            data = "file names:$fileNames"
        )

        context.files = fileNames
        context.zipFilesResponse = contentProvider.download(context)
        contextRepository?.save(context)
        return context.zipFilesResponse as Flux<DataBuffer>
    } catch (e: Throwable) {
        context.fail(
            errorCustom(
                code = "downloadError",
                field = "downloadError",
                group = "s3",
                message = e.message ?: "Unknown S3 error"
            )
        )
        contextRepository?.save(context)
        throw e
    }
}
