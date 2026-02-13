package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.flows

import io.mockk.*
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config.KafkaTopicsConfig
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.AnalysisSaveProcessor
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.AnalyzerProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AnalysisFlowConfigurerTest {

    private lateinit var analyzerProcessor: AnalyzerProcessor
    private lateinit var analysisSaveProcessor: AnalysisSaveProcessor
    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var analysisRepository: IsAnalysisRepository
    private lateinit var topologyTestDriver: TopologyTestDriver
    private lateinit var inputTopic: TestInputTopic<String, String>
    private lateinit var outputTopic: TestOutputTopic<String, String>

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        analysisRepository = mockk()

        analyzerProcessor = AnalyzerProcessor(lightContextRepository)
        analysisSaveProcessor = AnalysisSaveProcessor(analysisRepository, lightContextRepository)

        val builder = StreamsBuilder()
        val configurer = AnalysisFlowConfigurer(analyzerProcessor, analysisSaveProcessor)
        configurer.configure(builder)

        val topology = builder.build()
        val props = Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
        }
        topologyTestDriver = TopologyTestDriver(topology, props)

        inputTopic = topologyTestDriver.createInputTopic(
            KafkaTopicsConfig.ANALYSIS_CREATE_INPUT,
            Serdes.String().serializer(),
            Serdes.String().serializer()
        )

        outputTopic = topologyTestDriver.createOutputTopic(
            KafkaTopicsConfig.COMPOSITION_OUTPUT,
            Serdes.String().deserializer(),
            Serdes.String().deserializer()
        )
    }

    @AfterEach
    fun tearDown() {
        topologyTestDriver.close()
        clearAllMocks()
    }

    @Test
    fun `should process analysis flow end-to-end`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val compositionId = IsCompositionId("comp-123")
        val composition = IsComposition(id = compositionId, text = "test composition")
        val initialContext = IsLightContext(
            id = contextId,
            command = IsCommand.ANALYSIS_CREATE,
            composition = composition,
            lightCommands = mutableListOf()
        )
        val inputJson = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } returns Unit

        coEvery { analysisRepository.findAnalysisByCompositionId(compositionId) } returns null
        coEvery { analysisRepository.saveAnalysis(any()) } just Runs

        inputTopic.pipeInput("key", inputJson)

        assertFalse(outputTopic.isEmpty)
        val outputRecord = outputTopic.readRecord()
        val resultContext = commonLightContextDeserialize(outputRecord.value())

        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.ANALYZER))
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.ANALYSIS_SAVE))
        assertEquals(IsSubCommand.READY, resultContext.subCommand)
        assertNotNull(resultContext.analysis)
        assertEquals(compositionId, resultContext.analysis.compositionId)

        verify(atLeast = 1) { lightContextRepository.findById(contextId) }
        verify(atLeast = 2) { lightContextRepository.save(any()) }
        coVerify(exactly = 1) { analysisRepository.findAnalysisByCompositionId(compositionId) }
        coVerify(exactly = 1) { analysisRepository.saveAnalysis(any()) }
    }
}