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

class AnalysisSaveProcessorTest {

    private lateinit var analysisRepository: IsAnalysisRepository
    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var processor: AnalysisSaveProcessor

    @BeforeEach
    fun setUp() {
        analysisRepository = mockk()
        lightContextRepository = mockk()
        processor = AnalysisSaveProcessor(analysisRepository, lightContextRepository)
    }

    @Test
    fun `processAnalysisSave should save new analysis`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val compositionId = IsCompositionId("comp-123")
        val analysis = IsAnalysis(
            id = IsAnalysisId("analysis-1"),
            compositionId = compositionId
        )
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_CREATE,
            composition = IsComposition(id = compositionId),
            analysis = analysis,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { analysisRepository.findAnalysisByCompositionId(compositionId) } returns null
        coEvery { analysisRepository.saveAnalysis(any()) } just Runs
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.ANALYSIS_SAVE))
        assertEquals(IsSubCommand.READY, resultContext.subCommand)
        assertEquals(analysis.id, resultContext.analysis.id)

        verify(exactly = 1) { lightContextRepository.findById(contextId) }
        coVerify(exactly = 1) { analysisRepository.findAnalysisByCompositionId(compositionId) }
        coVerify(exactly = 1) { analysisRepository.saveAnalysis(any()) }
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processAnalysisSave should reuse existing analysis`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val compositionId = IsCompositionId("comp-123")
        val existingAnalysis = IsAnalysis(
            id = IsAnalysisId("existing"),
            compositionId = compositionId
        )
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_CREATE,
            composition = IsComposition(id = compositionId),
            analysis = IsAnalysis.NONE,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { analysisRepository.findAnalysisByCompositionId(compositionId) } returns existingAnalysis
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(existingAnalysis.id, resultContext.analysis.id)

        coVerify(exactly = 0) { analysisRepository.saveAnalysis(any()) }
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processAnalysisSave should handle regenerate command`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val compositionId = IsCompositionId("comp-123")
        val regenerateId = IsAnalysisId("new-analysis")
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_REGENERATE,
            regenerateId = regenerateId,
            composition = IsComposition(id = compositionId),
            analysis = IsAnalysis.NONE,
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { analysisRepository.saveAnalysis(any()) } just Runs
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(regenerateId, resultContext.analysis.id)

        coVerify(exactly = 0) { analysisRepository.findAnalysisByCompositionId(any()) }
        coVerify(exactly = 1) { analysisRepository.saveAnalysis(match { it.id == regenerateId }) }
    }

    @Test
    fun `processAnalysisSave should skip if already processed`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingContext = IsLightContext(
            id = contextId,
            lightCommands = mutableListOf(IsLightCommand.ANALYSIS_SAVE)
        )
        val jsonInput = commonLightContextSerialize(existingContext)

        every { lightContextRepository.findById(contextId) } returns existingContext

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        assertEquals(existingContext, commonLightContextDeserialize(jsonOutput))
        verify(exactly = 0) { lightContextRepository.save(any()) }
    }

    @Test
    fun `processAnalysisSave should handle FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val failingContext = IsLightContext(
            id = contextId,
            state = IsState.FAILING
        )
        val jsonInput = commonLightContextSerialize(failingContext)

        every { lightContextRepository.findById(contextId) } returns failingContext
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertFalse(resultContext.lightCommands.contains(IsLightCommand.ANALYSIS_SAVE))

        verify(exactly = 1) { lightContextRepository.save(failingContext) }
    }

    @Test
    fun `processAnalysisSave should handle exception and set FAILING state`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val compositionId = IsCompositionId("comp-123")
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_CREATE,
            composition = IsComposition(id = compositionId),
            lightCommands = mutableListOf()
        )
        val jsonInput = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { analysisRepository.findAnalysisByCompositionId(any()) } throws RuntimeException("DB error")
        every { lightContextRepository.save(any()) } returns Unit

        val jsonOutput = processor.processAnalysisSave(jsonInput, "key")

        val resultContext = commonLightContextDeserialize(jsonOutput)
        assertEquals(IsState.FAILING, resultContext.state)
        assertTrue(resultContext.errors.any { it.code == "SAVE_ERROR" })
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.ANALYSIS_SAVE))
        verify(exactly = 1) { lightContextRepository.save(any()) }
    }
}