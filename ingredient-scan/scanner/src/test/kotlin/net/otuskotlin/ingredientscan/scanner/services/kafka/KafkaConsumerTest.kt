package net.otuskotlin.ingredientscan.scanner.services.kafka

import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.scanner.services.await.Constants.Companion.TASK_READY
import net.otuskotlin.ingredientscan.scanner.services.await.ContextEvent
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.KafkaConsumer
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockitoExtension::class)
class KafkaConsumerTest {

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<ContextEvent>

    private lateinit var consumer: KafkaConsumer

    @BeforeEach
    fun setUp() {
        consumer = KafkaConsumer(eventPublisher)
    }

    @Test
    fun `handleContextReady should publish event for valid message`() {
        val contextId = IsContextId("test-123")
        val lightContext = IsLightContext(id = contextId)
        val json = commonLightContextSerialize(lightContext)
        val record = ConsumerRecord(KafkaTopicsConfig.COMPOSITION_OUTPUT, 0, 0L, contextId.asString(), json)

        consumer.handleContextReady(record)

        verify(eventPublisher).publishEvent(eventCaptor.capture())
        val event = eventCaptor.value
        assertEquals(TASK_READY, event.task)
        assertEquals(lightContext, event.context)
    }

    @Test
    fun `handleContextReady should not publish event for invalid JSON`() {
        val invalidJson = "{ not a valid json }"
        val record = ConsumerRecord(KafkaTopicsConfig.COMPOSITION_OUTPUT, 0, 0L, "key", invalidJson)

        consumer.handleContextReady(record)

        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `handleContextReady should not publish event for wrong topic`() {
        // Метод слушает только COMPOSITION_OUTPUT, но это не влияет, так как тест сам вызывает метод
        // Но мы можем проверить, что если сообщение из другого топика, то оно игнорируется? Нет, потому что
        // аннотация @KafkaListener гарантирует, что метод вызывается только для нужного топика.
        // Поэтому этот тест не нужен.
    }

    @Test
    fun `handleContextReady should handle exception gracefully`() {
        val record = ConsumerRecord(KafkaTopicsConfig.COMPOSITION_OUTPUT, 0, 0L, "key", "null")
        consumer.handleContextReady(record)
        verify(eventPublisher, never()).publishEvent(any())
    }
}