package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.analyzer.services.integration.ai.AIApiService
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
open class AnalyzerProcessor(
    @Qualifier("memoryLightContextRepo") private val lightContextRepository: IsLightContextRepository,
    private val aIApiService: AIApiService
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

            // Используем runBlocking, так как листенер Kafka работает в блокирующем режиме.
            // Поток потребителя (Consumer Thread) будет ожидать завершения обработки,
            // прежде чем закоммитить оффсет и перейти к следующему сообщению.
            runBlocking {
                context = aIApiService.aiAnalyzeCreate(context)
            }
            // Добавляем распознанный текст в контекст
            log.info("=== Analyzer completed ===\nanalysis: {}, errors: {}", context.analysis, context.errors)
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

}