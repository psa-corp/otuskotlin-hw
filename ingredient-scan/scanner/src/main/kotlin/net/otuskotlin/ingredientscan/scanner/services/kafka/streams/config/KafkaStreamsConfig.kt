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

//    companion object {
//        val COMPOSITION_CREATE_INPUT: String = KafkaTopicsConfig.COMPOSITION_CREATE_INPUT
//        val COMPOSITION_OUTPUT: String = KafkaTopicsConfig.COMPOSITION_OUTPUT
//        val OCR_RECOGNITION_INPUT: String = KafkaTopicsConfig.OCR_RECOGNITION_INPUT
//        val ANALYSIS_CREATE_INPUT: String = KafkaTopicsConfig.ANALYSIS_CREATE_INPUT
//    }
//    @Bean
//    open fun streamsBuilderCustomizer(): StreamsBuilderFactoryBeanCustomizer {
//        return StreamsBuilderFactoryBeanCustomizer { factoryBean ->
//            factoryBean.setInfrastructureCustomizer(object : KafkaStreamsInfrastructureCustomizer {
//                override fun configureBuilder(builder: StreamsBuilder) {
//                    log.info("Starting topology configuration...")
//                    if (topologyConfigurers.isEmpty()) {
//                        log.warn("No topology configurers found!")
//                    } else {
//                        topologyConfigurers.forEach { configurer ->
//                            log.info("Applying: {}", configurer::class.java.simpleName)
//                            configurer.configure(builder)
//                        }
//                    }
//                }
//            })
//        }
//    }
    @Bean
    open fun kStream(
        builder: StreamsBuilder, // Spring сам создаст и внедрит сюда builder
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