package net.otuskotlin.ingredientscan.logging.common

import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalStdlibApi::class)
@Suppress("unused")
interface IsLogWrapper: AutoCloseable {
    val loggerId: String

    fun log(
        msg: String = "",
        level: IsLogLevel = IsLogLevel.TRACE,
        marker: String = "DEV",
        e: Throwable? = null,
        data: Any? = null,
        objs: Map<String, Any>? = null,
    )

    fun error(
        msg: String = "",
        marker: String = "DEV",
        e: Throwable? = null,
        data: Any? = null,
        objs: Map<String, Any>? = null,
    ) = log(msg, IsLogLevel.ERROR, marker, e, data, objs)

    fun info(
        msg: String = "",
        marker: String = "DEV",
        data: Any? = null,
        objs: Map<String, Any>? = null,
    ) = log(msg, IsLogLevel.INFO, marker, null, data, objs)

    fun debug(
        msg: String = "",
        marker: String = "DEV",
        data: Any? = null,
        objs: Map<String, Any>? = null,
    ) = log(msg, IsLogLevel.DEBUG, marker, null, data, objs)

    @OptIn(ExperimentalTime::class)
    suspend fun <T> doWithLogging(
        id: String = "",
        level: IsLogLevel = IsLogLevel.INFO,
        block: suspend () -> T,
    ): T = try {
        log("Started $loggerId $id", level)
        val (res, diffTime) = measureTimedValue { block() }

        log(
            msg = "Finished $loggerId $id",
            level = level,
            objs = mapOf("metricHandleTime" to diffTime.toIsoString())
        )
        res
    } catch (e: Throwable) {
        log(
            msg = "Failed $loggerId $id",
            level = IsLogLevel.ERROR,
            e = e
        )
        throw e
    }

    suspend fun <T> doWithErrorLogging(
        id: String = "",
        throwRequired: Boolean = true,
        block: suspend () -> T,
    ): T? = try {
        val result = block()
        result
    } catch (e: Throwable) {
        log(
            msg = "Failed $loggerId $id",
            level = IsLogLevel.ERROR,
            e = e
        )
        if (throwRequired) throw e else null
    }

    override fun close() {}

    companion object {
        val DEFAULT = object: IsLogWrapper {
            override val loggerId: String = "NONE"

            override fun log(
                msg: String,
                level: IsLogLevel,
                marker: String,
                e: Throwable?,
                data: Any?,
                objs: Map<String, Any>?,
            ) {
                val markerString = marker
                    .takeIf { it.isNotBlank() }
                    ?.let { " ($it)" }
                val args = listOfNotNull(
                    "${Clock.System.now()} [${level.name}]$markerString: $msg",
                    e?.let { "${it.message ?: "Unknown reason"}:\n${it.stackTraceToString()}" },
                    data?.toString(),
                    objs?.toString(),
                )
                println(args.joinToString("\n"))
            }
        }
    }
}