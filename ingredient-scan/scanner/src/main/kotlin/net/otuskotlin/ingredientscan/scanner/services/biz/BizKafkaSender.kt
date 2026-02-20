package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig.Companion.ANALYSIS_CREATE_INPUT
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig.Companion.COMPOSITION_CREATE_INPUT
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig.Companion.OCR_RECOGNITION_INPUT
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BizKafkaSender(private val kafkaTemplate: KafkaTemplate<String, String>) : IsMessageSender {

    private val log = LoggerFactory.getLogger(BizKafkaSender::class.java)

    override suspend fun send(context: IsContext) {
        log.info("Sending context to Kafka")
        topicByCommand(context.subCommand).let { topic ->
            if (!topic.isEmpty()) {
                val lightContext = context.toLightContext()
                val json = commonLightContextSerialize(lightContext)
                log.info("Sending to topic: {}, key: {}", topic, lightContext.id.asString())
                kafkaTemplate.send(topic, lightContext.id.asString(), json)
                    .whenComplete { result, ex ->
                        if (ex != null) {
                            log.error("Failed to send message to Kafka: {}", ex.message)
                        } else {
                            log.info(
                                "Successfully sent message to Kafka. Topic: {}, Partition: {}",
                                result?.recordMetadata?.topic(),
                                result?.recordMetadata?.partition()
                            )
                        }
                    }
            }
        }
    }

    private fun topicByCommand (command: IsSubCommand) = when (command) {
        IsSubCommand.COMPOSITION_CREATE -> COMPOSITION_CREATE_INPUT
        IsSubCommand.OCR_RECOGNITION -> OCR_RECOGNITION_INPUT
        IsSubCommand.ANALYSIS_CREATE -> ANALYSIS_CREATE_INPUT
        else -> ""
    }
}