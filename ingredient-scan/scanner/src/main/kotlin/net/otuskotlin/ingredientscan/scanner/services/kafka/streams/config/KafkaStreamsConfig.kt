package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer

@Configuration
@EnableKafkaStreams
open class KafkaStreamsConfig(
    private val topologyConfigurers: List<TopologyConfigurer>
) {

    private val log = LoggerFactory.getLogger(KafkaStreamsConfig::class.java)

    @Bean
    open fun kStream(
        builder: StreamsBuilder,
        topologyConfigurers: List<TopologyConfigurer>
    ): KStream<String, String>? {
        log.info("BUILDING TOPOLOGY: Found {} configurers", topologyConfigurers.size)

        if (topologyConfigurers.isEmpty()) {
            log.error("CRITICAL: No topology configurers found. Streams will fail!")
            // Чтобы не падало, если пусто:
            // builder.stream<String, String>("dummy-topic")
        }

        topologyConfigurers.forEach { configurer ->
            log.info("Applying configurer: {}", configurer::class.java.simpleName)
            configurer.configure(builder)
        }

        return null
    }

}