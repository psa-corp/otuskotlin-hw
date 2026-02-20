package net.otuskotlin.ingredientscan.app.common

import net.otuskotlin.ingredientscan.api.log1.mapper.toLog
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.asIsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.external.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransport
import java.time.LocalDateTime
import kotlin.reflect.KClass

suspend inline fun <R : IResponse> IsAppSettings.submitHelper(
    request: IRequest,
    clazz: KClass<*>,
    logId: String,
): R {
    val logger = settings.loggerProvider.logger(clazz)
    val context = IsContext(
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
