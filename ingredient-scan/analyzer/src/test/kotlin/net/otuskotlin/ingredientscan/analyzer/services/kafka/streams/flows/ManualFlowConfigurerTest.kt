package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.flows

import io.mockk.*
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config.KafkaTopicsConfig
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.CompositionSaveProcessor
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.CompositionValidateProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class ManualFlowConfigurerTest {

    private lateinit var validateProcessor: CompositionValidateProcessor
    private lateinit var saveProcessor: CompositionSaveProcessor
    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var compositionRepository: IsCompositionRepository
    private lateinit var topologyTestDriver: TopologyTestDriver
    private lateinit var inputTopic: TestInputTopic<String, String>
    private lateinit var outputTopic: TestOutputTopic<String, String>

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        compositionRepository = mockk()

        validateProcessor = CompositionValidateProcessor(lightContextRepository)
        saveProcessor = CompositionSaveProcessor(compositionRepository, lightContextRepository)

        val builder = StreamsBuilder()
        val configurer = ManualFlowConfigurer(validateProcessor, saveProcessor)
        configurer.configure(builder)
        val topology = builder.build()

        val props = Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
        }
        topologyTestDriver = TopologyTestDriver(topology, props)

        inputTopic = topologyTestDriver.createInputTopic(
            KafkaTopicsConfig.COMPOSITION_CREATE_INPUT,
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
    fun `should process composition create flow end-to-end`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val scanText = "Test composition text"
        val lightContext = IsLightContext(
            id = contextId,
            scan = IsScan(text = scanText),
            lightCommands = mutableListOf()
        )
        val inputJson = commonLightContextSerialize(lightContext)

        every { lightContextRepository.findById(contextId) } returns null
        every { lightContextRepository.save(any()) } returns Unit

        coEvery { compositionRepository.findByText(scanText) } returns null
        coEvery { compositionRepository.save(any<IsComposition>()) } just Runs

        inputTopic.pipeInput("key", inputJson)

        val outputRecord = outputTopic.readRecord()
        val outputJson = outputRecord.value()
        val resultContext = commonLightContextDeserialize(outputJson)

        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_VALIDATION))
        assertTrue(resultContext.lightCommands.contains(IsLightCommand.COMPOSITION_SAVE))
        assertEquals(IsSubCommand.READY, resultContext.subCommand)
        assertEquals(scanText, resultContext.composition.text)

        verify(exactly = 2) { lightContextRepository.findById(contextId) }
        coVerify(exactly = 1) { compositionRepository.findByText(scanText) }
        coVerify(exactly = 1) { compositionRepository.save(any()) }
        verify(exactly = 2) { lightContextRepository.save(any()) }
    }
}