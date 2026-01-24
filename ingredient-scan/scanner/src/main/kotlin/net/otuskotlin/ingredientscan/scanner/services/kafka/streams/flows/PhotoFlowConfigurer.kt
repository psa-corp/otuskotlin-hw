package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.flows


import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionSaveProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionValidateProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.TopologyConfigurer
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors.OcrRecognitionProcessor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class PhotoFlowConfigurer(
    private val ocrProcessor: OcrRecognitionProcessor,
    private val validateProcessor: CompositionValidateProcessor,
    private val saveProcessor: CompositionSaveProcessor
) : TopologyConfigurer {

    private val log = LoggerFactory.getLogger(PhotoFlowConfigurer::class.java)

    override fun configure(streamsBuilder: StreamsBuilder) {
        log.info("Setting up OCR_RECOGNITION and COMPOSITION_CREATE pipeline...")

        val stringSerde = Serdes.StringSerde()
        val consumed = Consumed.with(stringSerde, stringSerde)
        val produced = Produced.with(stringSerde, stringSerde)

        // Validation & Save
        streamsBuilder.stream(KafkaStreamsConfig.Companion.OCR_RECOGNITION_INPUT, consumed)
            .mapValues { json ->
                log.info("OCR_RECOGNITION : OCR Processing - {}", json.take(100))
                ocrProcessor.processOcrRecognition(json, null)
            }
            .mapValues { json ->
                log.info("COMPOSITION_CREATE : Validating - {}", json.take(100))
                validateProcessor.processCompositionValidation(json, null)
            }
            .mapValues { json ->
                log.info("COMPOSITION_CREATE: Saving - {}", json.take(100))
                saveProcessor.processCompositionSave(json, null)
            }
            .to(KafkaStreamsConfig.Companion.COMPOSITION_OUTPUT, produced)

        log.info("COMPOSITION_CREATE_MANUAL pipeline ready")
    }
}
