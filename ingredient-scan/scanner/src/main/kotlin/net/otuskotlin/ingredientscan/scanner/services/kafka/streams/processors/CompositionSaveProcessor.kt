package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID.randomUUID

@Component
open class CompositionSaveProcessor(
    private val compositionRepository: InMemoryCompositionRepository,
    private val contextRepository: InMemoryContextRepository
) {

    private val log = LoggerFactory.getLogger(CompositionSaveProcessor::class.java)

    fun processCompositionSave(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Save started ===\nkey: {}", key)
        val lightContext = commonLightContextDeserialize(json)

        log.info(
            "Composition Save: Received light context:{}\n" +
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

        return try {

            log.info("Received context for saving:\n" +
                    " command: {}\n" +
                    " compositionText: {}",
                context.command,
                context.scan.text
            )

            val textToSave = context.scan.text

            // Идемпотентность
            val existingComposition = findOrCreateComposition(textToSave)

            log.info("Composition processed with ID: {}", existingComposition.id.asString())

            // Добавляем результат в ответ
            context.compositionResponse = existingComposition
            context.subCommand = IsSubCommand.READY

            log.info("=== Composition Save completed successfully ===")
            contextRepository.saveUnsuspend(context)
            return commonLightContextSerialize(context.toLightContext())
        } catch (e: Exception) {
            log.error("Error during composition save", e)
             context.fail(
                    IsError(
                        code = "SAVE_ERROR",
                        group = "SAVE_PROCESSOR",
                        field = "database",
                        message = "Failed to save composition: ${e.message}"
                    )
                )


            contextRepository.saveUnsuspend(context)
            commonLightContextSerialize(context.toLightContext())
        }
    }

    private fun findOrCreateComposition(text: String): IsComposition {
        log.info("Looking for existing composition with text: {}", text.take(30))

        val existing = compositionRepository.findByTextUnsuspend(text)

        return if (existing != null) {
            log.info("Found existing composition: ID = {}", existing.id.asString())
            existing
        } else {
            val newComposition = IsComposition(
                id = IsCompositionId("composition-${randomUUID()}"),
                text = text,
                createDate = LocalDateTime.now()
            )

            compositionRepository.saveUnsuspend(newComposition)
            log.info("Created new composition: ID = {}", newComposition.id.asString())
            newComposition
        }
    }
}
