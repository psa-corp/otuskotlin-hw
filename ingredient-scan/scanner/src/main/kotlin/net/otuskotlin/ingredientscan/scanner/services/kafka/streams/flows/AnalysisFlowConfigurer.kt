package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.flows


import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.AnalysisSaveProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionSaveProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.CompositionValidateProcessor
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.TopologyConfigurer
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors.AnalyzerProcessor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class AnalysisFlowConfigurer(
    private val analyzerProcessor: AnalyzerProcessor,
    private val analysisSaveProcessor: AnalysisSaveProcessor
) : TopologyConfigurer {

    private val log = LoggerFactory.getLogger(AnalysisFlowConfigurer::class.java)

    override fun configure(streamsBuilder: StreamsBuilder) {
        log.info("Setting up ANALYSIS_CREATE pipeline...")

        val stringSerde = Serdes.StringSerde()
        val consumed = Consumed.with(stringSerde, stringSerde)
        val produced = Produced.with(stringSerde, stringSerde)

        // Validation & Save
        streamsBuilder.stream(KafkaStreamsConfig.Companion.ANALYSIS_CREATE_INPUT, consumed)
            .mapValues { json ->
                log.info("ANALYSIS_CREATE : analyzer - {}", json.take(100))
                analyzerProcessor.processAnalyzer(json, null)
            }
            .mapValues { json ->
                log.info("ANALYSIS_CREATE : Saving - {}", json.take(100))
                analysisSaveProcessor.processAnalysisSave(json, null)
            }
            .to(KafkaStreamsConfig.Companion.COMPOSITION_OUTPUT, produced)

        log.info("ANALYSIS_CREATE pipeline ready")
    }
}
