package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
open class AnalyzerProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(AnalyzerProcessor::class.java)

    fun processAnalyzer(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Analyzer started ===\nkey: {}", key)
        val context = commonContextDeserialize(json)
        return try {

            log.info("Received context:\n" +
                    "  command: {}\n" +
                    "  composition: {}",
                context.command,
                context.composition
            )

            // STUB: Распознавание текста
            val analysis = performAnalyzer(context.composition)
            log.info("OCR recognized text: {}", analysis)

            // Добавляем распознанный текст в контекст
            context.analysisResponse = analysis

            context.state = IsState.RUNNING

            log.info("=== Analyzer completed ===\nanalysis: {}", analysis)

            contextRepository.saveUnsuspend(context)
            commonContextSerialize(context)

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
            contextRepository.saveUnsuspend(context)
            commonContextSerialize(errorContext)
        }
    }

    private fun performAnalyzer(composition: IsComposition): IsAnalysis {
        log.debug("Performing Analyzer on composition: {}", composition)

        // STUB DATA - тестовый текст состава
        val stub  = IsAnalysisStub.Companion.STUB_ANALYSIS

        log.info("Analyzer STUB: returning analysis")
        return stub
    }
}