package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsLightCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.external.models.IsLightContextRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class CompositionValidateProcessor(
    @Qualifier("memoryLightContextRepo") private val lightContextRepository: IsLightContextRepository
) {
    private val log = LoggerFactory.getLogger(CompositionValidateProcessor::class.java)
    fun processCompositionValidation(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Validate started ===\nkey: {}", key)
        var context = commonLightContextDeserialize(json)
        val con = lightContextRepository.findById(context.id)
        if (con != null) {
            if (con.lightCommands.contains(IsLightCommand.COMPOSITION_VALIDATION)) {
                log.info("=== Composition Validate Skip ===\n  LightContext ID:{}", con.id)
                return commonLightContextSerialize(con)
            }
            context = con
        }

        if (context.state == IsState.FAILING) {
            lightContextRepository.save(context)
            log.error("=== Composition Validate Error ===\n  LightContext ID:{}", context.id)
            return commonLightContextSerialize(context)
        }

        log.info("=== Composition Validate completed ===\nState: {}", context.state.name)
        validate(context)
        context.lightCommands.add(IsLightCommand.COMPOSITION_VALIDATION)
        lightContextRepository.save(context)

        return commonLightContextSerialize(context)
    }

    fun validate (context: IsLightContext) {
        log.info("=== Composition Validate started ===\ncontext: $context")
    }
}
