package net.otuskotlin.ingredientscan.libs.logging.common

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Инициализирует выбранный логер
 *
 * ```kotlin
 * // Обычно логер вызывается вот так
 * val logger = LoggerFactory.getLogger(this::class.java)
 * // Мы создаем экземпляр логер-провайдера вот так
 * val loggerProvider = MkpLoggerProvider { clazz -> mpLoggerLogback(clazz) }
 *
 * // В дальнейшем будем использовать этот экземпляр вот так:
 * val logger = loggerProvider.logger(this::class)
 * logger.info("My log")
 * ```
 */

class IsLoggerProvider(
    private val provider: (String) -> IsLogWrapper = { IsLogWrapper.DEFAULT }
) {
    fun logger(loggerId: String): IsLogWrapper = provider(loggerId)

    fun logger(clazz: KClass<*>): IsLogWrapper = provider(clazz.qualifiedName ?: clazz.simpleName ?: "(unknown)")

    fun logger(function: KFunction<*>): IsLogWrapper = provider(function.name)
}