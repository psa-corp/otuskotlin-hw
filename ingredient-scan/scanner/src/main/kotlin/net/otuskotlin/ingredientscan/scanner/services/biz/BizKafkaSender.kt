package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig.Companion.ANALYSIS_CREATE_INPUT
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig.Companion.COMPOSITION_CREATE_INPUT
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig.Companion.OCR_RECOGNITION_INPUT
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BizKafkaSender(private val kafkaTemplate: KafkaTemplate<String, String>) : IsMessageSender {

    private val log = LoggerFactory.getLogger(BizKafkaSender::class.java)

    override suspend fun send(context: IsContext) {
        log.info("Sending context to Kafka topic: {}", COMPOSITION_CREATE_INPUT)
        topicByCommand(context.subCommand)?.let {topic ->
            val contextJson = commonContextSerialize(context)
            kafkaTemplate.send(topic, context.id.asString(), contextJson)
        }
    }

    private fun topicByCommand (command: IsSubCommand) = when (command) {
        IsSubCommand.COMPOSITION_CREATE -> COMPOSITION_CREATE_INPUT
        IsSubCommand.OCR_RECOGNITION -> OCR_RECOGNITION_INPUT
        IsSubCommand.ANALYSIS_CREATE -> ANALYSIS_CREATE_INPUT
        else -> null
    }
}