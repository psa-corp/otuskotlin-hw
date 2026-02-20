package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.external.models.IsLightContextRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class OcrRecognitionProcessor(
    @Qualifier("memoryLightContextRepo") private val lightContextRepository: IsLightContextRepository
) {
    private val log = LoggerFactory.getLogger(OcrRecognitionProcessor::class.java)

    fun processOcrRecognition(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== OCR Recognition started ===\nkey: {}", key)

        var context = commonLightContextDeserialize(json)
        try {
            val con = lightContextRepository.findById(context.id)
            if (con != null) {
                if (con.lightCommands.contains(IsLightCommand.OCR_RECOGNITION)) {
                    log.info("=== OCR Recognition Skip ===\n  LightContext ID:{}", con.id)
                    return commonLightContextSerialize(con)
                }
                context = con
            }

            if (context.state == IsState.FAILING) {
                lightContextRepository.save(context)
                log.error("=== OCR Recognition Error ===\n  LightContext ID:{}", context.id)
                return commonLightContextSerialize(context)
            }

            val recognizedText = performOcrRecognition(context.scan.files)
            log.info("OCR Recognition text: {}", recognizedText)

            context.scan.text = recognizedText

            log.info("=== OCR Recognition completed ===\nRecognized text: {}", recognizedText)
            context.lightCommands.add(IsLightCommand.OCR_RECOGNITION)
            lightContextRepository.save(context)
            return commonLightContextSerialize(context)
        } catch (e: Exception) {
            log.error("Error during OCR recognition", e)

            context.fail(
                IsError(
                    code = "OCR_RECOGNITION_ERROR",
                    group = "OCR_PROCESSOR",
                    field = "ocr",
                    message = "Failed to process OCR: ${e.message}"
                )
            )
            context.lightCommands.add(IsLightCommand.OCR_RECOGNITION)
            lightContextRepository.save(context)
            return commonLightContextSerialize(context)
        }
    }

    private fun performOcrRecognition(photoUrls: MutableList<String>): String {
        log.debug("Performing OCR recognition on photos: {}", photoUrls)

        // STUB DATA - тестовый текст состава
        val stubCompositionText = STUB_COMPOSITION.text

        log.info("OCR STUB: returning test composition text")
        return stubCompositionText
    }
}