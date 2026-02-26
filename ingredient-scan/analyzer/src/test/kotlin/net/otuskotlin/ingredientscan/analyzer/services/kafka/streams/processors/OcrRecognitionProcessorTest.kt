package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import io.mockk.*
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class OcrRecognitionProcessorTest {

    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var processor: OcrRecognitionProcessor

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        processor = OcrRecognitionProcessor(lightContextRepository)
    }

    @Test
    fun `processOcrRecognition should perform OCR and update context`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val scan = IsScan(files = mutableListOf("photo1.jpg"))
        val initialContext = IsLightContext(
            id = contextId,
            scan = scan,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processOcrRecognition(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.OCR_RECOGNITION))
        assertEquals(IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text, resultContext.scan.text)

        verify(exactly = 1) { lightContextRepository.findById(contextId) }
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processOcrRecognition should skip if already processed`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingContext = IsLightContext(
            id = contextId,
            lightCommands = mutableListOf(IsLightCommand.OCR_RECOGNITION)
        )
        val jsonInput = commonLightContextSerialize(existingContext)

        every { lightContextRepository.findById(contextId) } returns existingContext

        val jsonOutput = processor.processOcrRecognition(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(existingContext, resultContext)

        verify(exactly = 0) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processOcrRecognition should handle FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val failingContext = IsLightContext(
            id = contextId,
            state = IsState.FAILING,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(failingContext)

        every { lightContextRepository.findById(contextId) } returns failingContext
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processOcrRecognition(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertFalse(resultContext.lightCommands.contains(IsLightCommand.OCR_RECOGNITION))

        verify(exactly = 1) { lightContextRepository.save(failingContext) }
    }

    @Test
    fun `processOcrRecognition should handle exception and set FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val initialContext = IsLightContext(
            id = contextId,
            scan = IsScan(files = mutableListOf("photo.jpg")),
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } throws RuntimeException("DB error") andThen Unit

        val jsonOutput = processor.processOcrRecognition(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertTrue(resultContext.errors.isNotEmpty())
        assertTrue(resultContext.errors.any { it.code == "OCR_RECOGNITION_ERROR" })
        verify(exactly = 2) { lightContextRepository.save(any()) }
    }
}