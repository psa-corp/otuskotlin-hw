package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.scanner.services.await.Constants.Companion.TASK_READY
import net.otuskotlin.ingredientscan.scanner.services.await.ContextEvent
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaStreamsConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener

class KafkaConsumer(private val appEventPublisher: ApplicationEventPublisher,) {
    private val log = LoggerFactory.getLogger(KafkaConsumer::class.java)

    @KafkaListener(
        topics = [KafkaStreamsConfig.Companion.COMPOSITION_OUTPUT],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleContextReady(message: ConsumerRecord<String, String>) {
        log.info("Received context ready for key: ${message.key()}")

        try {
            val context = commonContextDeserialize(message.value())
            appEventPublisher.publishEvent(ContextEvent(context, TASK_READY))
            log.info("Published ContextEvent for ${context.id}")
        } catch (e: Exception) {
            log.error("Failed to process context ready message: ${message.value()}", e)
        }
    }

}