package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams

@Configuration
@EnableKafkaStreams
@ConditionalOnProperty(
name = ["spring.kafka.streams.auto-startup"],
havingValue = "true",
//matchIfMissing = true
)
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
    open fun streamsBuilder(): StreamsBuilder = StreamsBuilder()

    @Bean
    open fun buildCompositionTopology(streamsBuilder: StreamsBuilder): Topology {
        topologyConfigurers.forEach { configurer ->
            configurer.configure(streamsBuilder)
        }
        return streamsBuilder.build()
    }
}