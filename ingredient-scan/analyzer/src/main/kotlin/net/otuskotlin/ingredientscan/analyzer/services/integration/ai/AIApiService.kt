package net.otuskotlin.ingredientscan.analyzer.services.integration.ai

import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.AnalyzerProcessor
import net.otuskotlin.ingredientscan.core.common.ai.AICompositionRequest
import net.otuskotlin.ingredientscan.core.common.ai.AiAnalysis
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.mappers.toTransport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID.randomUUID

@Service
class AIApiService(private val aIApiClient: AIApiClient) {
    private val log = LoggerFactory.getLogger(AnalyzerProcessor::class.java)
    suspend fun aiAnalyzeCreate(context: IsLightContext): IsLightContext {

        log.debug("Analyzer on composition: {}", context.composition)

        val response: AiAnalysis = aIApiClient.aiAnalyzeCreate(
            request = AICompositionRequest(context.composition.text)
        )

        log.info("Analyzer on composition: response: {}", response)

        val analysis = IsAnalysis(
            id = IsAnalysisId("analysis-${randomUUID()}"),
            compositionId = context.composition.id,
            createDate = LocalDateTime.now(),
            description =  response.description,
            rating = response.rating,
            color = response.color,
            components = response.components.map { it.toTransport() }.toMutableList()
        )

        log.info("Analyzer on composition: analysis: {}", analysis)

        context.analysis = analysis
        if (!response.errors.isEmpty()) {
            context.fail( response.errors)
        }
        return context
    }
}
