package net.otuskotlin.ingredientscan.scanner.services.kafka.streams

import net.otuskotlin.ingredientscan.core.common.external.helpers.errorContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import net.otuskotlin.ingredientscan.scanner.services.await.Constants.Companion.TASK_READY
import net.otuskotlin.ingredientscan.scanner.services.await.ContextEvent
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(private val appEventPublisher: ApplicationEventPublisher,
                    private val contextRepository: InMemoryContextRepository) {
    private val log = LoggerFactory.getLogger(KafkaConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopicsConfig.COMPOSITION_OUTPUT],
        groupId = "\${spring.kafka.consumer.group-id}",
//        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleContextReady(message: ConsumerRecord<String, String>) {
        log.info("Received context ready for key: ${message.key()}")

        try {
            val lightContext = commonLightContextDeserialize(message.value())
            appEventPublisher.publishEvent(ContextEvent(lightContext, TASK_READY))
            log.info("Published ContextEvent for ${lightContext.id}")
        } catch (e: Exception) {
            log.error("Failed to process context ready message: ${message.value()}", e)
        }
    }

}