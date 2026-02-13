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

class AnalyzerProcessorTest {

    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var processor: AnalyzerProcessor

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        processor = AnalyzerProcessor(lightContextRepository)
    }

    @Test
    fun `processAnalyzer should perform analysis and update context`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val composition = IsComposition(
            id = IsCompositionId("comp-123"),
            text = "test composition"
        )
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_CREATE,
            composition = composition,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalyzer(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.ANALYZER))
        assertNotNull(resultContext.analysis)
        assertEquals(composition.id, resultContext.analysis.compositionId)

        verify(exactly = 1) { lightContextRepository.findById(contextId) }
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processAnalyzer should skip if already processed`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingContext = IsLightContext(
            id = contextId,
            lightCommands = mutableListOf(IsLightCommand.ANALYZER)
        )
        val jsonInput = commonLightContextSerialize(existingContext)

        every { lightContextRepository.findById(contextId) } returns existingContext

        val jsonOutput = processor.processAnalyzer(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(existingContext, resultContext)
        verify(exactly = 0) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processAnalyzer should handle FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val failingContext = IsLightContext(
            id = contextId,
            state = IsState.FAILING,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(failingContext)

        every { lightContextRepository.findById(contextId) } returns failingContext
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalyzer(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertFalse(resultContext.lightCommands.contains(IsLightCommand.ANALYZER))

        verify(exactly = 1) { lightContextRepository.save(failingContext) }
    }

    @Test
    fun `processAnalyzer should handle exception and set FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val initialContext = IsLightContext(
            id = contextId,
            composition = IsComposition(id = IsCompositionId("comp")),
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } throws RuntimeException("DB error") andThen Unit

        val jsonOutput = processor.processAnalyzer(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertTrue(resultContext.errors.isNotEmpty())
        assertTrue(resultContext.errors.any { it.code == "ANALYZER" })
        verify(exactly = 2) { lightContextRepository.save(any()) }
    }
}