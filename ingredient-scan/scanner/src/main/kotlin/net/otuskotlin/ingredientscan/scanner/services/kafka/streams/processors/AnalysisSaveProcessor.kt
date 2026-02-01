package net.otuskotlin.ingredientscan.scanner.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryAnalysisRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryLightContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component


@Component
open class AnalysisSaveProcessor(
    private val analysisRepository: InMemoryAnalysisRepository,
    private val lightContextRepository: InMemoryLightContextRepository
) {

    private val log = LoggerFactory.getLogger(AnalysisSaveProcessor::class.java)

    fun processAnalysisSave(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Analysis Save started ===\nkey: {}", key)
        var context = commonLightContextDeserialize(json)
        val con = lightContextRepository.findById(context.id)
        if (con != null) {
            if (con.lightCommands.contains(IsLightCommand.ANALYSIS_SAVE)) {
                log.info("=== Analysis Save Skip ===\n  LightContext ID:{}", con.id)
                return commonLightContextSerialize(con)
            }
            context = con
        }

        if (context.state == IsState.FAILING) {
            lightContextRepository.save(context)
            log.error("=== Analysis Save Error ===\n  LightContext ID:{}", context.id)
            return commonLightContextSerialize(context)
        }
        return try {

            if (context.command == IsCommand.ANALYSIS_REGENERATE) {
                context.analysis.id = context.regenerateId
                saveAnalysis(context.analysis)
            } else {
                // Идемпотентность
                val existingAnalysis = findAnalysisByCompositionId(context.composition.id)
                if (existingAnalysis != null) {
                    context.analysis = existingAnalysis
                } else {
                    saveAnalysis(context.analysis)
                }
            }

            log.info("=== Analysis Save completed successfully ===")
            context.subCommand = IsSubCommand.READY
            context.lightCommands.add(IsLightCommand.ANALYSIS_SAVE)
            lightContextRepository.save(context)
            commonLightContextSerialize(context)

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
            context.lightCommands.add(IsLightCommand.ANALYSIS_SAVE)
            lightContextRepository.save(context)
            commonLightContextSerialize(context)
        }
    }

    fun saveAnalysis(analysis: IsAnalysis) {
        log.info("=== Analysis Save ===")
        analysisRepository.saveAnalysisUnsuspend(analysis)
    }

    fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis? {
        log.info("=== Analysis Find ===")
        return analysisRepository.findAnalysisByCompositionIdUnsuspend(id)
    }

}
