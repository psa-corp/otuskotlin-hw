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

class CompositionSaveProcessorTest {

    private lateinit var compositionRepository: IsCompositionRepository
    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var processor: CompositionSaveProcessor

    @BeforeEach
    fun setUp() {
        compositionRepository = mockk()
        lightContextRepository = mockk()
        processor = CompositionSaveProcessor(compositionRepository, lightContextRepository)
    }

    @Test
    fun `processCompositionSave should save new composition and update context`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val scanText = "Test composition text"
        val initialContext = IsLightContext(
            id = contextId,
            scan = IsScan(text = scanText),
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null

        coEvery { compositionRepository.findByText(scanText) } returns null
        coEvery { compositionRepository.save(any<IsComposition>()) } just Runs

        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processCompositionSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)

        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_SAVE))
        assertEquals(IsSubCommand.READY, resultContext.subCommand)
        assertNotNull(resultContext.composition)
        assertEquals(scanText, resultContext.composition.text)

        verify(exactly = 1) { lightContextRepository.findById(contextId) }
        coVerify(exactly = 1) { compositionRepository.findByText(scanText) }
        coVerify(exactly = 1) { compositionRepository.save(any<IsComposition>()) }
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processCompositionSave should skip if already processed`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingContext = IsLightContext(
            id = contextId,
            scan = IsScan(text = "some text"),
            lightCommands = mutableListOf(IsLightCommand.COMPOSITION_SAVE),
            composition = IsComposition(id = IsCompositionId("existing"))
        )
        val jsonInput = commonLightContextSerialize(existingContext)

        every { lightContextRepository.findById(contextId) } returns existingContext

        val jsonOutput = processor.processCompositionSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(existingContext, resultContext)

        verify(exactly = 1) { lightContextRepository.findById(contextId) }

        coVerify(exactly = 0) { compositionRepository.findByText(any()) }
        coVerify(exactly = 0) { compositionRepository.save(any()) }
        verify(exactly = 0) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processCompositionSave should handle FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val failingContext = IsLightContext(
            id = contextId,
            state = IsState.FAILING,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(failingContext)

        every { lightContextRepository.findById(contextId) } returns failingContext
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processCompositionSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertFalse(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_SAVE))

        verify(exactly = 1) { lightContextRepository.save(failingContext) }
    }

    @Test
    fun `processCompositionSave should handle exception during findOrCreate`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val scanText = "Test"
        val initialContext = IsLightContext(
            id = contextId,
            scan = IsScan(text = scanText),
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { compositionRepository.findByText(scanText) } throws RuntimeException("DB error")
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processCompositionSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertTrue(resultContext.errors.any { it.code == "SAVE_ERROR" })
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_SAVE))

        coVerify(exactly = 1) { compositionRepository.findByText(scanText) }
        coVerify(exactly = 0) { compositionRepository.save(any()) }
        verify(exactly = 1) { lightContextRepository.save(match { it.state == IsState.FAILING }) }
    }
}