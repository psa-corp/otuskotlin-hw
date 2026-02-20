package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors

import io.mockk.*
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class CompositionValidateProcessorTest {

    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var processor: CompositionValidateProcessor

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        processor = CompositionValidateProcessor(lightContextRepository)
    }

    @Test
    fun `processCompositionValidation should validate and update context`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val initialContext = IsLightContext(
            id = contextId,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processCompositionValidation(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_VALIDATION))

        verify(exactly = 1) { lightContextRepository.save(match { it.id == contextId }) }
    }

    @Test
    fun `processCompositionValidation should skip if already validated`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingContext = IsLightContext(
            id = contextId,
            lightCommands = mutableListOf(IsLightCommand.COMPOSITION_VALIDATION)
        )
        val jsonInput = commonLightContextSerialize(existingContext)

        every { lightContextRepository.findById(contextId) } returns existingContext

        val jsonOutput = processor.processCompositionValidation(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(existingContext, resultContext)

        verify(exactly = 0) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processCompositionValidation should handle FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val failingContext = IsLightContext(
            id = contextId,
            state = IsState.FAILING
        )
        val jsonInput = commonLightContextSerialize(failingContext)

        every { lightContextRepository.findById(contextId) } returns failingContext
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processCompositionValidation(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertFalse(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_VALIDATION))

        verify(exactly = 1) { lightContextRepository.save(failingContext) }
    }
}