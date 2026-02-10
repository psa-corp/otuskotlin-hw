package net.otuskotlin.ingredientscan.app.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.biz.common.IsBizInternalProcessor
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@SpringBootTest
class InternalSubmitHelperTest {

    @MockitoBean
    private lateinit var processor: IsBizInternalProcessor

    @MockitoBean
    private lateinit var settings: IsCorSettings

    @Test
    fun `internalSubmitHelper should process analysis find request`() = runTest {
        // Given
        val request = InternalAnalysisFindRequest(
            compositionId = "composition-123",
            requestType = "internalAnalysisFind"
        )
        val logId = "test-log-id"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<InternalContext>(0)
            context.state = IsState.FINISHING
            context.analysisResponse = IsAnalysis(
                id = IsAnalysisId("analysis-123"),
                compositionId = IsCompositionId("composition-123"),
                createDate = LocalDateTime.now(),
                description = "Test analysis",
                rating = 4.5,
                color = IsColor.GREEN,
                problematicComponents = mutableListOf(),
                safeComponents = mutableListOf()
            )
        }

        // When
        val result: InternalAnalysisFindResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalAnalysisFind", result.responseType)
        assertEquals(InternalResponseResult.SUCCESS, result.result)
        assertNull(result.errors)
        assertNotNull(result.analysis)
        assertEquals("analysis-123", result.analysis?.id)
        assertEquals("composition-123", result.analysis?.compositionId)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should process analysis save request`() = runTest {
        // Given
        val request = InternalAnalysisSaveRequest(
            analysis = InternalAnalysis(
                id = "analysis-456",
                compositionId = "composition-456",
                createDate = LocalDateTime.now().atOffset(ZoneOffset.UTC),
                description = "Saved analysis",
                rating = 3.5,
                color = InternalColor.YELLOW,
                problematicComponent = mutableListOf(),
                safeComponent = mutableListOf()
            ),
            requestType = "internalAnalysisSave"
        )
        val logId = "analysis-save-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<InternalContext>(0)
            context.state = IsState.FINISHING
            context.analysisResponse = IsAnalysis(
                id = IsAnalysisId("analysis-456"),
                compositionId = IsCompositionId("composition-456"),
                createDate = LocalDateTime.now(),
                description = "Saved analysis",
                rating = 3.5,
                color = IsColor.YELLOW,
                problematicComponents = mutableListOf(),
                safeComponents = mutableListOf()
            )
        }

        // When
        val result: InternalAnalysisSaveResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalAnalysisSave", result.responseType)
        assertEquals(InternalResponseResult.SUCCESS, result.result)
        assertNull(result.errors)
        assertNotNull(result.analysis)
        assertEquals("analysis-456", result.analysis?.id)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should process composition find request`() = runTest {
        // Given
        val request = InternalCompositionFindRequest(
            text = "Соль, сахар, вода",
            requestType = "internalCompositionFind"
        )
        val logId = "composition-find-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<InternalContext>(0)
            context.state = IsState.FINISHING
            context.compositionResponse = IsComposition(
                id = IsCompositionId("composition-789"),
                createDate = LocalDateTime.now(),
                text = "Соль, сахар, вода"
            )
        }

        // When
        val result: InternalCompositionFindResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalCompositionFind", result.responseType)
        assertEquals(InternalResponseResult.SUCCESS, result.result)
        assertNull(result.errors)
        assertNotNull(result.composition)
        assertEquals("composition-789", result.composition?.id)
        assertEquals("Соль, сахар, вода", result.composition?.text)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should process composition save request`() = runTest {
        // Given
        val request = InternalCompositionSaveRequest(
            composition = InternalComposition(
                id = "composition-999",
                createDate = LocalDateTime.now().atOffset(ZoneOffset.UTC),
                text = "Молоко, мука, яйца"
            ),
            requestType = "internalCompositionSave"
        )
        val logId = "composition-save-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<InternalContext>(0)
            context.state = IsState.FINISHING
            context.compositionResponse = IsComposition(
                id = IsCompositionId("composition-999"),
                createDate = LocalDateTime.now(),
                text = "Молоко, мука, яйца"
            )
        }

        // When
        val result: InternalCompositionSaveResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalCompositionSave", result.responseType)
        assertEquals(InternalResponseResult.SUCCESS, result.result)
        assertNull(result.errors)
        assertNotNull(result.composition)
        assertEquals("composition-999", result.composition?.id)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should handle processor exception`() = runTest {
        // Given
        val request = InternalAnalysisFindRequest(
            compositionId = "composition-err",
            requestType = "internalAnalysisFind"
        )
        val logId = "error-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor to throw exception
        Mockito.doThrow(RuntimeException("Processor failed"))
            .`when`(processor).exec(any())

        // When
        val result: InternalAnalysisFindResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalAnalysisFind", result.responseType)
        assertEquals(InternalResponseResult.ERROR, result.result)
        assertNotNull(result.errors)
        assertTrue(result.errors?.isNotEmpty() == true)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should return error response when state is FAILING`() = runTest {
        // Given
        val request = InternalCompositionFindRequest(
            text = "test",
            requestType = "internalCompositionFind"
        )
        val logId = "failing-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // Mock processor to set FAILING state
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<InternalContext>(0)
            context.state = IsState.FAILING
            context.errors.add(
                IsError(
                    code = "INTERNAL_ERROR",
                    message = "Database connection failed"
                )
            )
        }

        // When
        val result: InternalCompositionFindResponse = appSettings.internalSubmitHelper(
            request = request,
            clazz = InternalSubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("internalCompositionFind", result.responseType)
        assertEquals(InternalResponseResult.ERROR, result.result)
        assertNotNull(result.errors)
        assertEquals(1, result.errors?.size)

        Mockito.verify(processor).exec(any())
    }

    @Test
    fun `internalSubmitHelper should handle InternalErrorResponse for unknown request type`() = runTest {

        val request = object : InternalRequest {
            override val requestType: String = "unknownRequestType"
        }

        val logId = "unknown-request-test"

        // Create appSettings implementation
        val appSettings = object : IsInternalAppSettings {
            override val processor: IsBizInternalProcessor = this@InternalSubmitHelperTest.processor
            override val settings: IsCorSettings = this@InternalSubmitHelperTest.settings
        }

        // When - Then
        assertThrows(ClassCastException::class.java) {
            runTest {
                appSettings.internalSubmitHelper<InternalErrorResponse>(
                    request = request,
                    clazz = InternalSubmitHelperTest::class,
                    logId = logId
                )
            }
        }
    }
}