package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class OcrRecognitionProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(OcrRecognitionProcessor::class.java)

    fun processOcrRecognition(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== OCR Recognition started ===\nkey: {}", key)

        val lightContext = commonLightContextDeserialize(json)

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

        val recognizedText = performOcrRecognition(context.scanRequest.files)
        log.info("OCR recognized text: {}", recognizedText)

        context.compositionRequest.text = recognizedText


        log.info("=== OCR Recognition completed ===\nRecognized text: {}", recognizedText)

        contextRepository.saveUnsuspend(context)
        return commonLightContextSerialize(context.toLightContext())
    }

    private fun performOcrRecognition(photoUrls: MutableList<String>): String {
        log.debug("Performing OCR recognition on photos: {}", photoUrls)

        // STUB DATA - тестовый текст состава
        val stubCompositionText = STUB_COMPOSITION.text

        log.info("OCR STUB: returning test composition text")
        return stubCompositionText
    }
}