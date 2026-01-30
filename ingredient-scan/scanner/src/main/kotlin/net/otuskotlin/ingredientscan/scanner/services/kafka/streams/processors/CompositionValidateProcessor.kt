package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class CompositionValidateProcessor(
    private val contextRepository: InMemoryContextRepository,
    private val messageSource: MessageSource
) {

    private val log = LoggerFactory.getLogger(CompositionValidateProcessor::class.java)

    fun processCompositionValidation(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Validate started ===\nkey: {}", key)
        var lightContext = commonLightContextDeserialize(json)

        log.info(
            "Composition Validate: Received light context:{}\n" +
                    " command: {}\n" +
                    " state: {}\n:",
            lightContext.id,
            lightContext.command,
            lightContext.state
        )

        val context = contextRepository.findByIdUnsuspend(lightContext.id)
        if (context == null || context.state == IsState.FAILING) {
            if (context == null) {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context not found to Repos. id:${lightContext.id.asString()} : CompositionValidateProcessor"
                    )
                )
            } else {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context error state. id:${lightContext.id.asString()} : CompositionValidateProcessor"
                    )
                )
            }
            log.error("=== Composition Validate error ===\n  LightContext ID:{}", lightContext.id)
            return commonLightContextSerialize(lightContext)
        }

        log.info(
            "Composition Validate: Received context:{}\n" +
                    " command: {}\n",
            context.id,
            context.command)

        log.info("=== Composition Validate completed ===\nState: {}", context.state.name)
        validate(context)
        contextRepository.saveUnsuspend(context)

        return commonLightContextSerialize(context.toLightContext())
    }

    fun validate (context: IsContext) {
        log.info("=== Composition Validate started ===\ncontext: $context")
    }
}
