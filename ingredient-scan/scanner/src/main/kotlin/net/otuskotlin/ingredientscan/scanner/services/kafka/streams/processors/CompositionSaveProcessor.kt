package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.mappers.apiContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class CompositionSaveProcessor() {

    private val log = LoggerFactory.getLogger(CompositionSaveProcessor::class.java)

    fun processCompositionSave(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Save started ===\nkey: {}", key)

        return try {
            val context = apiContextDeserialize(json)

            // Проверяем: есть ли ошибки от предыдущих процессоров
            if (context.errors.isNotEmpty()) {
                log.warn("Skipping save due to errors:\n{}",
                    context.errors.map { "${it.code}: ${it.message}" }.joinToString("\n")
                )
                context.state = IsState.FAILING
                return commonContextSerialize(context)
            }

            log.debug("Received context for saving:\n" +
                    "  command: {}\n" +
                    "  compositionText: {}",
                context.command,
                context.compositionRequest.text.take(50) + "..."
            )

            // STUB: Сохранение в БД
            val savedComposition = saveToDatabase(context.compositionRequest)

            log.info("Composition saved with ID: {}", savedComposition.id.asString())

            // Добавляем сохранённый состав в ответ
            context.compositionResponse = savedComposition
            context.state = IsState.FINISHING

            log.info("=== Composition Save completed successfully ===")

            commonContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during composition save", e)
            val errorContext = IsContext().apply {
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
            commonContextSerialize(errorContext)
        }
    }

    private fun saveToDatabase(composition: IsComposition): IsComposition {
        log.debug("STUB: Saving composition to database")

        // STUB: Сохранение (в реальном приложении здесь repository.save())
        val saved = STUB_COMPOSITION

        log.info("STUB: Composition saved with ID: {}", saved.id.asString())
        return saved
    }
}