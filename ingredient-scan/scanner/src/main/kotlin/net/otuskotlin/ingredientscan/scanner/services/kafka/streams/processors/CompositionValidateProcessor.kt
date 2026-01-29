package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
open class CompositionValidateProcessor(private val contextRepository: InMemoryContextRepository) {

    private val log = LoggerFactory.getLogger(CompositionValidateProcessor::class.java)

    fun processCompositionValidation(
        @Payload json: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ): String {
        log.info("=== Composition Validate started ===\nkey: {}", key)


        val context = commonContextDeserialize(json)

        log.info(
            "Received context:\n" +
                    " command: {}\n" +
                    " text: {}",
            context.command,
            context.compositionRequest.text.take(50) + "..."
        )

        context.state = IsState.RUNNING

        log.info("=== Composition Validate completed ===\nState: {}", context.state.name)
        contextRepository.saveUnsuspend(context)
        return commonContextSerialize(context)
    }
}
