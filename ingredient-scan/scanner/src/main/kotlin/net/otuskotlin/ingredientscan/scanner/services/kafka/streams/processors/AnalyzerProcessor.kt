package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
open class AnalyzerProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(AnalyzerProcessor::class.java)

    fun processAnalyzer(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Analyzer started ===\nkey: {}", key)
        val lightContext = commonLightContextDeserialize(json)
        val context = contextRepository.findByIdUnsuspend(lightContext.id)
        if (context == null || context.state == IsState.FAILING) {
            if (context == null) {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context not found to Repos. id:${lightContext.id.asString()} : AnalyzerProcessor"
                    )
                )
            } else {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context error state. id:${lightContext.id.asString()} : AnalyzerProcessor"
                    )
                )
            }
            log.error("=== Analyzer error ===\n  LightContext ID:{}", lightContext.id)
            return commonLightContextSerialize(lightContext)
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
            context.analysisResponse = analysis
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
        val stub = IsAnalysisStub.Companion.STUB_ANALYSIS
        stub.id = IsAnalysisId("analysis-${randomUUID()}")
        stub.compositionId = composition.id

        log.info("Analyzer STUB: returning analysis")
        return stub
    }
}