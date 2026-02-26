package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.flows

import io.mockk.*
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config.KafkaTopicsConfig
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.CompositionSaveProcessor
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.CompositionValidateProcessor
import net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.processors.OcrRecognitionProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonLightContextSerialize
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PhotoFlowConfigurerTest {

    private lateinit var lightContextRepository: IsLightContextRepository
    private lateinit var compositionRepository: IsCompositionRepository
    private lateinit var topologyTestDriver: TopologyTestDriver
    private lateinit var inputTopic: TestInputTopic<String, String>
    private lateinit var outputTopic: TestOutputTopic<String, String>

    @BeforeEach
    fun setUp() {
        lightContextRepository = mockk()
        compositionRepository = mockk()

        val ocrProcessor = OcrRecognitionProcessor(lightContextRepository)
        val validateProcessor = CompositionValidateProcessor(lightContextRepository)
        val saveProcessor = CompositionSaveProcessor(compositionRepository, lightContextRepository)

        val builder = StreamsBuilder()
        val configurer = PhotoFlowConfigurer(ocrProcessor, validateProcessor, saveProcessor)
        configurer.configure(builder)

        val topology = builder.build()
        val props = Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
        }
        topologyTestDriver = TopologyTestDriver(topology, props)

        inputTopic = topologyTestDriver.createInputTopic(
            KafkaTopicsConfig.OCR_RECOGNITION_INPUT,
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
    fun `should process photo flow end-to-end with new composition`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val scan = IsScan(files = mutableListOf("photo1.jpg", "photo2.jpg"))
        val initialContext = IsLightContext(
            id = contextId,
            scan = scan,
            lightCommands = mutableListOf()
        )
        val inputJson = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { compositionRepository.findByText(IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text) } returns null
        coEvery { compositionRepository.save(any<IsComposition>()) } just Runs
        every { lightContextRepository.save(any()) } returns Unit

        inputTopic.pipeInput("key", inputJson)

        assertFalse(outputTopic.isEmpty)
        val outputRecord = outputTopic.readRecord()
        val resultContext = commonLightContextDeserialize(outputRecord.value())

        assertEquals(contextId, resultContext.id)
        assertTrue(resultContext.lightCommands.containsAll(listOf(
            IsLightCommand.OCR_RECOGNITION,
            IsLightCommand.COMPOSITION_VALIDATION,
            IsLightCommand.COMPOSITION_SAVE
        )))
        assertEquals(IsSubCommand.READY, resultContext.subCommand)
        assertEquals(IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text, resultContext.scan.text)
        assertNotNull(resultContext.composition)
        assertEquals(resultContext.scan.text, resultContext.composition.text)

        verify(atLeast = 1) { lightContextRepository.findById(contextId) }
        verify(atLeast = 1) { lightContextRepository.save(any()) }
        coVerify(exactly = 1) { compositionRepository.findByText(IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text) }
        coVerify(exactly = 1) { compositionRepository.save(any<IsComposition>()) }
    }

    @Test
    fun `should reuse existing composition when text matches`() {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val existingComposition = IsComposition(
            id = IsCompositionId("comp-123"),
            text = IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text
        )
        val scan = IsScan(files = mutableListOf("photo.jpg"))
        val initialContext = IsLightContext(
            id = contextId,
            scan = scan,
            lightCommands = mutableListOf()
        )
        val inputJson = commonLightContextSerialize(initialContext)

        every { lightContextRepository.findById(contextId) } returns null
        coEvery { compositionRepository.findByText(IsCompositionStub.STUB_COMPOSITION_COLA_DOBRA.text) } returns existingComposition
        every { lightContextRepository.save(any()) } returns Unit

        inputTopic.pipeInput("key", inputJson)

        val outputRecord = outputTopic.readRecord()
        val resultContext = commonLightContextDeserialize(outputRecord.value())

        assertEquals(existingComposition.id, resultContext.composition.id)
        coVerify(exactly = 0) { compositionRepository.save(any<IsComposition>()) }
    }
}