package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaStreamsConfig(
    private val topologyConfigurers: List<TopologyConfigurer>
) {

    private val log = LoggerFactory.getLogger(KafkaStreamsConfig::class.java)

    companion object {
        const val COMPOSITION_CREATE_INPUT = "composition-create-input"
        const val OCR_RECOGNITION_INPUT = "ocr-recognition-input"
        const val COMPOSITION_VALIDATE_INPUT = "composition-validate-input"
        const val COMPOSITION_OUTPUT = "composition-output"
    }

    @Bean
    open fun buildCompositionTopology(streamsBuilder: StreamsBuilder): Topology {
        log.info("═══════════════════════════════════════════════════════════════════════════════")
        log.info("Building Composition Kafka Streams Topology")
        log.info("Found {} topology configurers", topologyConfigurers.size)
        log.info("═══════════════════════════════════════════════════════════════════════════════")

        topologyConfigurers.forEach { configurer ->
            configurer.configure(streamsBuilder)
        }

        log.info("═══════════════════════════════════════════════════════════════════════════════")
        log.info("Composition Kafka Streams Topology built successfully")
        log.info("═══════════════════════════════════════════════════════════════════════════════")

        return streamsBuilder.build()
    }
}