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
class CompositionValidateProcessor() {

    private val log = LoggerFactory.getLogger(CompositionValidateProcessor::class.java)


    fun processCompositionValidation(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Validate started ===\nkey: {}", key)

        return try {
            val context = apiContextDeserialize(json)

            log.info("Received context:\n" +
                    "  command: {}\n" +
                    "  text: {}",
                context.command,
                context.compositionRequest.text.take(50) + "..."
            )

            val isDuplicate = checkForDuplicate(context.compositionRequest.text)

            if (isDuplicate) {
                log.error("Composition is duplicate - already exists in database")
                context.errors.add(
                    IsError(
                        code = "COMPOSITION_DUPLICATE",
                        group = "VALIDATION",
                        field = "composition.text",
                        message = "Composition with this text already exists"
                    )
                )
                context.state = IsState.FAILING
            } else {
                log.debug("Composition is unique - proceeding to save")
                val composition = parseComposition(context.compositionRequest.text)
                context.compositionRequest = composition

                context.command = IsCommand.COMPOSITION_CREATE_MANUAL
                context.state = IsState.RUNNING
            }

            log.info("=== Composition Validate completed ===\nState: {}", context.state.name)

            commonContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during composition validation", e)
            val errorContext = IsContext().apply {
                errors.add(
                    IsError(
                        code = "VALIDATION_ERROR",
                        group = "VALIDATE_PROCESSOR",
                        field = "validation",
                        message = "Validation failed: ${e.message}"
                    )
                )
                state = IsState.FAILING
            }
            commonContextSerialize(errorContext)
        }
    }

    private fun checkForDuplicate(text: String): Boolean {
        log.debug("STUB: Checking for duplicate compositions in database")

        // STUB: Проверяем по простому правилу (для примера)
        // В реальном приложении здесь будет запрос в БД
        val isDuplicate = false // Пока всегда возвращаем false

        log.info("STUB: Duplicate check result: {}", isDuplicate)
        return isDuplicate
    }

    private fun parseComposition(text: String): IsComposition {
        log.debug("STUB: Parsing composition text")
        // STUB: Сохранение (в реальном приложении здесь repository.save())
        val saved = STUB_COMPOSITION

        log.info("STUB: Composition saved with ID: {}", saved.id.asString())
        return saved
    }
}