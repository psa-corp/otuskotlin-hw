package net.otuskotlin.ingredientscan.app.internal

import net.otuskotlin.ingredientscan.api.log1.mapper.toLog
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponse
import net.otuskotlin.ingredientscan.core.common.external.InternalContext

import net.otuskotlin.ingredientscan.core.common.external.helpers.asIsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import java.time.LocalDateTime
import kotlin.reflect.KClass

suspend inline fun <R : InternalResponse> IsInternalAppSettings.submitHelper(
    request: InternalRequest,
    clazz: KClass<*>,
    logId: String,
): R {
    val logger = settings.loggerProvider.logger(clazz)
    val context = InternalContext(
        timeStart = LocalDateTime.now(),
        state = IsState.RUNNING
    )
    return try {
        context.fromTransport(request)
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
