package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
open class CompositionValidateProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(CompositionValidateProcessor::class.java)

    fun processCompositionValidation(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Validate started ===\nkey: {}", key)
        val context = commonContextDeserialize(json)

        log.info(
            "Received context:\n" +
                    " command: {}\n" +
                    " text: {}",
            context.command,
            context.compositionRequest.text.take(50) + "..."
        )

        context.state = IsState.RUNNING

        log.info("=== Composition Validate completed ===\nState: {}", context.state.name)
        contextRepository.saveUnsuspend(context)
        return commonContextSerialize(context)
    }

//    private fun validateCompositionText(text: String): List<IsError> {
//        val errors = mutableListOf<IsError>()
//
//        log.info("Validating composition text: length = {}", text.length)
//
//        // Проверка: Текст не пустой
//        if (text.isBlank()) {
//            log.error("Validation failed: text is empty")
//            errors.add(
//                IsError(
//                    code = "EMPTY_TEXT",
//                    group = "VALIDATION",
//                    field = "composition.text",
//                    message = "Composition text cannot be empty"
//                )
//            )
//            return errors
//        }
//
//        // Проверка: Минимальная длина
//        if (text.length < 3) {
//            log.error("Validation failed: text too short (length = {})", text.length)
//            errors.add(
//                IsError(
//                    code = "TEXT_TOO_SHORT",
//                    group = "VALIDATION",
//                    field = "composition.text",
//                    message = "Composition text is too short (minimum 3 characters)"
//                )
//            )
//        }
//
//        // Проверка: Максимальная длина
//        if (text.length > 10_000) {
//            log.error("Validation failed: text too long (length = {})", text.length)
//            errors.add(
//                IsError(
//                    code = "TEXT_TOO_LONG",
//                    group = "VALIDATION",
//                    field = "composition.text",
//                    message = "Composition text exceeds maximum length (10000 characters)"
//                )
//            )
//        }
//
//        log.info("Validation result: errors = {}", errors.size)
//        return errors
//    }

    private fun normalizeText(text: String): String = text.replace("\\s+".toRegex(), " ").trim()

}
