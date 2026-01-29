package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryAnalysisRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component


@Component
open class AnalysisSaveProcessor(
    private val analysisRepository: InMemoryAnalysisRepository,
    private val contextRepository: InMemoryContextRepository
) {

    private val log = LoggerFactory.getLogger(AnalysisSaveProcessor::class.java)

    fun processAnalysisSave(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Analysis Save started ===\nkey: {}", key)
        val lightContext = commonLightContextDeserialize(json)
        val context = contextRepository.findByIdUnsuspend(lightContext.id)
        if (context == null || context.state == IsState.FAILING) {
            if (context == null) {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context not found to Repos. id:${lightContext.id.asString()} : AnalysisSaveProcessor"
                    )
                )
            } else {
                lightContext.fail(
                    errorContext(
                        violationCode = "kafka-processor",
                        message = "Context error state. id:${lightContext.id.asString()} : AnalysisSaveProcessor"
                    )
                )
            }
            log.error("=== Analysis Save error ===\n  LightContext ID:{}", lightContext.id)
            return commonLightContextSerialize(lightContext)
        }
        return try {

            // Проверяем: есть ли ошибки от предыдущих процессоров
            if (context.errors.isNotEmpty()) {
                log.error("Skipping save due to errors:\n{}",
                    context.errors.map { "${it.code}: ${it.message}" }.joinToString("\n")
                )
                context.state = IsState.FAILING
                contextRepository.saveUnsuspend(context)
                return commonContextSerialize(context)
            }

            log.info("Received context for saving:\n" +
                    " command: {}\n" +
                    " compositionText: {}",
                context.command,
                context.analysisRequest.description.take(100) + "..."
            )

            // Идемпотентность
            val existingAnalysis = analysisRepository.findAnalysisByCompositionIdUnsuspend(context.analysis.compositionId)

             if (existingAnalysis != null && context.analysis == IsAnalysis.NONE) {
                 context.analysisResponse = existingAnalysis
             }else{
                 context.analysisResponse.id = context.analysis.id
             }

            analysisRepository.saveAnalysisUnsuspend(context.analysisResponse)

            log.info("Analysis processed with ID: {}", context.analysisResponse)


            log.info("=== Analysis Save completed successfully ===")
            context.subCommand = IsSubCommand.READY
            contextRepository.saveUnsuspend(context)
            commonLightContextSerialize(context.toLightContext())

        } catch (e: Exception) {
            log.error("Error during analysis save", e)

                context.fail(
                    IsError(
                        code = "SAVE_ERROR",
                        group = "SAVE_PROCESSOR",
                        field = "database",
                        message = "Failed to save analysis: ${e.message}"
                    )
                )

            contextRepository.saveUnsuspend(context)
            commonLightContextSerialize(context.toLightContext())
        }
    }

}
