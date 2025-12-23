package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository

import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
open class OcrRecognitionProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(OcrRecognitionProcessor::class.java)

    fun processOcrRecognition(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== OCR Recognition started ===\nkey: {}", key)
        val context = commonContextDeserialize(json)
        return try {

            log.info("Received context:\n" +
                    "  command: {}\n" +
                    "  photoUrls: {}",
                context.command,
                context.scanRequest.text
            )

            // STUB: Распознавание текста
            val recognizedText = performOcrRecognition(context.scanRequest.text)
            log.info("OCR recognized text: {}", recognizedText)

            // Добавляем распознанный текст в контекст
            context.compositionRequest.text = recognizedText

            // Устанавливаем новую команду для следующего процессора
            context.command = IsCommand.COMPOSITION_CREATE_MANUAL
            context.state = IsState.RUNNING

            log.info("=== OCR Recognition completed ===\nRecognized text: {}", recognizedText)

            contextRepository.save(context.id.asString(), context)
            commonContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during OCR recognition", e)
            val errorContext = context.apply {
                errors.add(
                    IsError(
                        code = "OCR_ERROR",
                        group = "OCR_PROCESSOR",
                        field = "recognition",
                        message = "OCR recognition failed: ${e.message}"
                    )
                )
                state = IsState.FAILING
            }
            contextRepository.save(context.id.asString(), context)
            commonContextSerialize(errorContext)
        }
    }

    private fun performOcrRecognition(photoUrls: String): String {
        log.debug("Performing OCR recognition on photos: {}", photoUrls)

        // STUB DATA - тестовый текст состава
        val stubCompositionText = STUB_COMPOSITION.text

        log.info("OCR STUB: returning test composition text")
        return stubCompositionText
    }
}