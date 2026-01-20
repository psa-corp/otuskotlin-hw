package net.otuskotlin.ingredientscan.app.content

import net.otuskotlin.ingredientscan.api.log1.mapper.toLog
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.asIsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import java.time.LocalDateTime
import kotlin.reflect.KClass
import reactor.core.publisher.Flux
import org.springframework.http.codec.multipart.FilePart

suspend inline fun <R : IResponse> IsAppSettings.uploadHelper(
    request: IRequest,
    photos: Flux<FilePart>,
    clazz: KClass<*>,
    logId: String,
): R {
    val logger = settings.loggerProvider.logger(clazz)
    val context = IsContext(
        timeStart = LocalDateTime.now(),
    )
    return try {

        val contentProvider = settings.contentProvider
            ?: throw IllegalStateException("Content provider is not configured")

        val names = contentProvider.uploadFlux(context, photos, null)
        context.fromTransport(request, names.toMutableList())

        logger.info(
            msg = "Request $logId started for ${clazz.simpleName}",
            marker = "BIZ",
            data = context.toLog(logId)
        )

        processor.exec(context)

        logger.info(
            msg = "Request $logId processed for ${clazz.simpleName}",
            marker = "BIZ",
            data = context.toLog(logId)
        )

        subProcessor.exec(context)

        logger.info(
            msg = "Request $logId sub processed for ${clazz.simpleName}",
            marker = "BIZ",
            data = context.toLog(logId)
        )
        context.toTransport() as R

    } catch (e: Throwable) {
        logger.error(
            msg = "Request $logId failed for ${clazz.simpleName}",
            marker = "BIZ",
            data = context.toLog(logId),
            e = e,
        )
        context.state = IsState.FAILING
        context.errors.add(e.asIsError())
        context.toTransport() as R
    }
}
