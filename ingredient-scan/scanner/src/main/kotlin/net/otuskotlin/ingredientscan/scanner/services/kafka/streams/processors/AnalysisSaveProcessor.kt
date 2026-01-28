package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryAnalysisRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
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
        val context = commonContextDeserialize(json)
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

            log.debug("Received context for saving:\n" +
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

            // Добавляем результат в ответ

            context.state = IsState.FINISHING

            log.info("=== Analysis Save completed successfully ===")
            contextRepository.saveUnsuspend(context)
            commonContextSerialize(context)

        } catch (e: Exception) {
            log.error("Error during analysis save", e)
            val errorContext = context.apply {
                errors.add(
                    IsError(
                        code = "SAVE_ERROR",
                        group = "SAVE_PROCESSOR",
                        field = "database",
                        message = "Failed to save analysis: ${e.message}"
                    )
                )
                state = IsState.FAILING
            }
            contextRepository.saveUnsuspend(context)
            commonContextSerialize(errorContext)
        }
    }

}
