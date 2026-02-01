package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.analyzer.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.analyzer.repositories.InMemoryLightContextRepository
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
    private val lightContextRepository: InMemoryLightContextRepository,
) {

    private val log = LoggerFactory.getLogger(CompositionSaveProcessor::class.java)

    fun processCompositionSave(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Save started ===\nkey: {}", key)
        var context = commonLightContextDeserialize(json)
        val con = lightContextRepository.findById(context.id)
        if (con != null) {
            if (con.lightCommands.contains(IsLightCommand.COMPOSITION_SAVE)) {
                log.info("=== Composition Save Skip ===\n  LightContext ID:{}", con.id)
                return commonLightContextSerialize(con)
            }
            context = con
        }

        if (context.state == IsState.FAILING) {
            lightContextRepository.save(context)
            log.error("=== Composition Save Error ===\n  LightContext ID:{}", context.id)
            return commonLightContextSerialize(context)
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
            context.composition = existingComposition
            context.subCommand = IsSubCommand.READY
            context.lightCommands.add(IsLightCommand.COMPOSITION_SAVE)
            log.info("=== Composition Save completed successfully ===")
            lightContextRepository.save(context)
            return commonLightContextSerialize(context )
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

            context.lightCommands.add(IsLightCommand.COMPOSITION_SAVE)
            lightContextRepository.save(context)
            commonLightContextSerialize(context)
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
