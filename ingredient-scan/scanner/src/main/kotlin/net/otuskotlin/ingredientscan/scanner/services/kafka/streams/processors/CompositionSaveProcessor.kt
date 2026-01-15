package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
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
        val context = commonContextDeserialize(json)
        return try {

            // Проверяем: есть ли ошибки от предыдущих процессоров
            if (context.errors.isNotEmpty()) {
                log.error("Skipping save due to errors:\n{}",
                    context.errors.map { "${it.code}: ${it.message}" }.joinToString("\n")
                )
                context.state = IsState.FAILING
                contextRepository.save(context)
                return commonContextSerialize(context)
            }

            log.debug("Received context for saving:\n" +
                    " command: {}\n" +
                    " compositionText: {}",
                context.command,
                context.compositionRequest.text.take(50) + "..."
            )

            val textToSave = context.compositionRequest.text

            // Идемпотентность
            val existingComposition = findOrCreateComposition(textToSave)

            log.info("Composition processed with ID: {}", existingComposition.id.asString())

            // Добавляем результат в ответ
            context.compositionResponse = existingComposition
            context.state = IsState.FINISHING

            log.info("=== Composition Save completed successfully ===")
            contextRepository.save(context)
            commonContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during composition save", e)
            val errorContext = context.apply {
                errors.add(
                    IsError(
                        code = "SAVE_ERROR",
                        group = "SAVE_PROCESSOR",
                        field = "database",
                        message = "Failed to save composition: ${e.message}"
                    )
                )
                state = IsState.FAILING
            }
            contextRepository.save(context)
            commonContextSerialize(errorContext)
        }
    }

    private fun findOrCreateComposition(text: String): IsComposition {
        log.info("Looking for existing composition with text: {}", text.take(30))

        val existing = compositionRepository.findByText(text)

        return if (existing != null) {
            log.info("Found existing composition: ID = {}", existing.id.asString())
            existing
        } else {
            val newComposition = IsComposition(
                id = IsCompositionId(UUID.randomUUID().toString()),
                text = text,
                createDate = LocalDateTime.now()
            )

            compositionRepository.save(newComposition)

            log.info("Created new composition: ID = {}", newComposition.id.asString())
            newComposition
        }
    }
}
