package net.otuskotlin.ingredientscan.scanner.services.kafka

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.services.biz.BizKafkaSender
import net.otuskotlin.ingredientscan.scanner.services.kafka.streams.config.KafkaTopicsConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.UUID
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
class BizKafkaSenderTest {

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Captor
    private lateinit var topicCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var keyCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var valueCaptor: ArgumentCaptor<String>

    private lateinit var sender: BizKafkaSender

    @BeforeEach
    fun setUp() {
        sender = BizKafkaSender(kafkaTemplate)
    }

    @Test
    fun `send should send to COMPOSITION_CREATE_INPUT for COMPOSITION_CREATE command`() = runTest {
        val context = createContext(subCommand = IsSubCommand.COMPOSITION_CREATE)
        val lightContext = context.toLightContext()
        val expectedJson = commonLightContextSerialize(lightContext)
        val sendResult = Mockito.mock(SendResult::class.java) as SendResult<String, String>

        Mockito.`when`(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(sendResult))

        sender.send(context)

        Mockito.verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture())
        assertEquals(KafkaTopicsConfig.COMPOSITION_CREATE_INPUT, topicCaptor.value)
        assertEquals(lightContext.id.asString(), keyCaptor.value)
        assertEquals(expectedJson, valueCaptor.value)
    }

    @Test
    fun `send should send to OCR_RECOGNITION_INPUT for OCR_RECOGNITION command`() = runTest {
        val context = createContext(subCommand = IsSubCommand.OCR_RECOGNITION)
        val lightContext = context.toLightContext()
        val expectedJson = commonLightContextSerialize(lightContext)
        val sendResult = Mockito.mock(SendResult::class.java) as SendResult<String, String>

        Mockito.`when`(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(sendResult))

        sender.send(context)

        Mockito.verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture())
        assertEquals(KafkaTopicsConfig.OCR_RECOGNITION_INPUT, topicCaptor.value)
        assertEquals(lightContext.id.asString(), keyCaptor.value)
        assertEquals(expectedJson, valueCaptor.value)
    }

    @Test
    fun `send should send to ANALYSIS_CREATE_INPUT for ANALYSIS_CREATE command`() = runTest {
        val context = createContext(subCommand = IsSubCommand.ANALYSIS_CREATE)
        val lightContext = context.toLightContext()
        val expectedJson = commonLightContextSerialize(lightContext)
        val sendResult = Mockito.mock(SendResult::class.java) as SendResult<String, String>

        Mockito.`when`(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(sendResult))

        sender.send(context)

        Mockito.verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture())
        assertEquals(KafkaTopicsConfig.ANALYSIS_CREATE_INPUT, topicCaptor.value)
        assertEquals(lightContext.id.asString(), keyCaptor.value)
        assertEquals(expectedJson, valueCaptor.value)
    }

    @Test
    fun `send should not send for unknown command`() = runTest {
        val context = createContext(subCommand = IsSubCommand.NONE)

        sender.send(context)

        Mockito.verify(kafkaTemplate, Mockito.never()).send(anyString(), anyString(), anyString())
    }

    @Test
    fun `send should handle send failure gracefully`() = runTest {
        val context = createContext(subCommand = IsSubCommand.COMPOSITION_CREATE)
        val future = CompletableFuture<SendResult<String, String>>()
        future.completeExceptionally(RuntimeException("Kafka error"))

        Mockito.`when`(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(future)

        sender.send(context)

        Mockito.verify(kafkaTemplate).send(anyString(), anyString(), anyString())
    }

    private fun createContext(subCommand: IsSubCommand): IsContext {
        return IsContext(
            id = IsContextId("test-${UUID.randomUUID()}"),
            subCommand = subCommand
        )
    }
}