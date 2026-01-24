package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BizKafkaSender(private val kafkaTemplate: KafkaTemplate<String, String>) : IsMessageSender {

    private val log = LoggerFactory.getLogger(BizKafkaSender::class.java)
    companion object {
        const val COMPOSITION_CREATE_TOPIC = "composition-create-input"
        const val OCR_RECOGNITION_TOPIC = "ocr-recognition-input"
        const val COMPOSITION_VALIDATE_TOPIC = "composition-validate-input"
        const val COMPOSITION_SAVE_TOPIC = "composition-save-input"
    }

    override suspend fun send(context: IsContext) {
        log.info("Sending context to Kafka topic: {}", COMPOSITION_CREATE_TOPIC)
        topicByCommand(context.subCommand)?.let {topic ->
            val contextJson = commonContextSerialize(context)
            kafkaTemplate.send(topic, context.id.asString(), contextJson)
        }
    }

    private fun topicByCommand (command: IsSubCommand) = when (command) {
        IsSubCommand.COMPOSITION_CREATE -> COMPOSITION_CREATE_TOPIC
        IsSubCommand.OCR_RECOGNITION -> OCR_RECOGNITION_TOPIC
        else -> null
    }
}