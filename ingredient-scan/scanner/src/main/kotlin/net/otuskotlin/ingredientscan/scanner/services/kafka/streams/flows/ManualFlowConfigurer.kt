package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.flows


import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionSaveProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionValidateProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.TopologyConfigurer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("kafkaAdmin")
open class ManualFlowConfigurer(
    private val validateProcessor: CompositionValidateProcessor,
    private val saveProcessor: CompositionSaveProcessor
) : TopologyConfigurer {

    private val log = LoggerFactory.getLogger(ManualFlowConfigurer::class.java)

    override fun configure(streamsBuilder: StreamsBuilder) {
        log.info("Setting up COMPOSITION_CREATE pipeline...")

        val stringSerde = Serdes.String()
        val consumed = Consumed.with(stringSerde, stringSerde)
        val produced = Produced.with(stringSerde, stringSerde)

        // Validation & Save
        streamsBuilder.stream(KafkaTopicsConfig.COMPOSITION_CREATE_INPUT, consumed)
            .mapValues { json ->
                log.info("COMPOSITION_CREATE : Validating - {}", json.take(100))
                validateProcessor.processCompositionValidation(json, null)
            }
            .mapValues { json ->
                log.info("COMPOSITION_CREATE: Saving - {}", json.take(100))
                saveProcessor.processCompositionSave(json, null)
            }
            .to(KafkaTopicsConfig.COMPOSITION_OUTPUT, produced)

        log.info("COMPOSITION_CREATE pipeline ready")
        log.info("Input topic: {}", KafkaTopicsConfig.COMPOSITION_CREATE_INPUT)
        log.info("Output topic: {}", KafkaTopicsConfig.COMPOSITION_OUTPUT)
    }
}
