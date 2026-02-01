package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryLightContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
open class AnalyzerProcessor(
    private val lightContextRepository: InMemoryLightContextRepository
) {
    private val log = LoggerFactory.getLogger(AnalyzerProcessor::class.java)

    fun processAnalyzer(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Analyzer started ===\nkey: {}", key)
        var context = commonLightContextDeserialize(json)
        val con = lightContextRepository.findById(context.id)
        if (con != null) {
            if (con.lightCommands.contains(IsLightCommand.ANALYZER)) {
                log.info("=== Analyzer Skip ===\n  LightContext ID:{}", con.id)
                return commonLightContextSerialize(con)
            }
            context = con
        }

        if (context.state == IsState.FAILING) {
            lightContextRepository.save(context)
            log.error("=== Analyzer Error ===\n  LightContext ID:{}", context.id)
            return commonLightContextSerialize(context)
        }
        return try {

            log.info("Received context:\n" +
                    "  command: {}\n" +
                    "  composition: {}",
                context.command,
                context.composition
            )

            // STUB: Распознавание текста
            val analysis = performAnalyzer(context.composition)

            // Добавляем распознанный текст в контекст
            context.analysis = analysis
            log.info("=== Analyzer completed ===\nanalysis: {}", analysis)
            context.lightCommands.add(IsLightCommand.ANALYZER)
            lightContextRepository.save(context)
            commonLightContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during analyzer", e)
            val errorContext = context.apply {
                errors.add(
                    IsError(
                        code = "ANALYZER",
                        group = "ANALYZER_PROCESSOR",
                        field = "analyzer",
                        message = "Analyzer failed: ${e.message}"
                    )
                )
                state = IsState.FAILING
            }
            context.lightCommands.add(IsLightCommand.ANALYZER)
            lightContextRepository.save(context)
            commonLightContextSerialize(errorContext)
        }
    }

    private fun performAnalyzer(composition: IsComposition): IsAnalysis {
        log.debug("Performing Analyzer on composition: {}", composition)

        // STUB DATA - тестовый текст состава
        val stub = IsAnalysisStub.STUB_ANALYSIS
        stub.id = IsAnalysisId("analysis-${randomUUID()}")
        stub.compositionId = composition.id

        log.info("Analyzer STUB: returning analysis")
        return stub
    }
}