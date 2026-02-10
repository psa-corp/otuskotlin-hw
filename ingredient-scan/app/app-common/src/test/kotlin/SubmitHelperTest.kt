package net.otuskotlin.ingredientscan.app.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@SpringBootTest
class SubmitHelperTest {

    @MockitoBean
    private lateinit var processor: IsBizProcessor

    @MockitoBean
    private lateinit var subProcessor: IsBizSubProcessor

    @MockitoBean
    private lateinit var settings: IsCorSettings

    @Test
    fun `submitHelper should process composition get request with valid ID format`() = runTest {
        // Given
        val request = CompositionGetRequest(
            compositionId = "composition-123test",
            requestType = "compositionGet",
        )
        val logId = "test-log-id"

        // Create appSettings implementation with mocked dependencies
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock context repository
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)
        Mockito.`when`(settings.contextRepository).thenReturn(mockContextRepository)
        Mockito.doNothing().`when`(mockContextRepository).save(any())

        // Mock processors
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
            context.compositionResponse = IsComposition(
                id = IsCompositionId("composition-123test"),
                createDate = LocalDateTime.now(),
                text = "Test composition"
            )
        }

        Mockito.doNothing().`when`(subProcessor).exec(any())

        // When
        val result: CompositionGetResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("compositionGet", result.responseType)
        assertEquals(ResponseResult.SUCCESS, result.result)
        assertNull(result.errors)
        assertNotNull(result.composition)
        assertEquals("composition-123test", result.composition?.id)

        // Verify interactions
        Mockito.verify(processor).exec(any())
        Mockito.verify(subProcessor).exec(any())
        Mockito.verify(mockContextRepository).save(any())
    }

    @Test
    fun `submitHelper should handle processor exception`() = runTest {
        // Given
        val request = AnalysisGetRequest(
            analysisId = "analysis-123test",
            requestType = "analysisGet",
        )
        val logId = "test-log-id"

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock repository
        Mockito.`when`(settings.contextRepository).thenReturn(null)

        // Mock processor to throw exception
        Mockito.doThrow(RuntimeException("Processor failed"))
            .`when`(processor).exec(any())

        // When
        val result: AnalysisGetResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("analysisGet", result.responseType)
        assertEquals(ResponseResult.ERROR, result.result)
        assertNotNull(result.errors)
        assertTrue(result.errors?.isNotEmpty() == true)

        // Verify subProcessor was NOT called
        Mockito.verify(subProcessor, Mockito.never()).exec(any())
    }

    @Test
    fun `submitHelper should not call subProcessor when errors exist`() = runTest {
        // Given
        val request = CompositionCreateByManualRequest(
            scan = ScanManualDto(
                id = "scan-123",
                type = ScanType.MANUAL,
                text = "test ingredients list"
            ),
            requestType = "compositionCreateByManual",
        )
        val logId = "test-log-id"

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock repository
        Mockito.`when`(settings.contextRepository).thenReturn(null)

        // Mock processor to add errors
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
            context.errors.add(
                IsError(
                    code = "VALIDATION_ERROR",
                    message = "Invalid scan data"
                )
            )
        }

        // When
        val result: CompositionCreateByManualResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)

        // Verify subProcessor was NOT called because errors exist
        Mockito.verify(processor).exec(any())
        Mockito.verify(subProcessor, Mockito.never()).exec(any())
    }

    @Test
    fun `submitHelper should handle analysis create request`() = runTest {
        // Given
        val request = AnalysisCreateRequest(
            compositionId = "composition-456test",
            requestType = "analysisCreate",
        )
        val logId = "analysis-create-test"

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock context repository
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)
        Mockito.`when`(settings.contextRepository).thenReturn(mockContextRepository)
        Mockito.doNothing().`when`(mockContextRepository).save(any())

        // Mock processors
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
        }

        Mockito.doNothing().`when`(subProcessor).exec(any())

        // When
        val result: AnalysisCreateResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("analysisCreate", result.responseType)
        assertEquals(ResponseResult.SUCCESS, result.result)

        Mockito.verify(processor).exec(any())
        Mockito.verify(subProcessor).exec(any())
        Mockito.verify(mockContextRepository).save(any())
    }

    @Test
    fun `submitHelper should handle composition context get request`() = runTest {
        // Given
        val request = CompositionContextGetRequest(
            contextId = "context-789test",
            requestType = "compositionContextGet",
        )
        val logId = "context-get-test"

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock repository
        Mockito.`when`(settings.contextRepository).thenReturn(null)

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
            context.compositionContextResponse = IsCompositionContext(
                id = IsContextId("context-789test"),
                state = IsState.FINISHING,
                composition = IsComposition(
                    id = IsCompositionId("composition-789"),
                    text = "Test composition for context"
                )
            )
        }

        Mockito.doNothing().`when`(subProcessor).exec(any())

        // When
        val result: CompositionContextGetResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("compositionContextGet", result.responseType)
        assertEquals(ResponseResult.SUCCESS, result.result)
    }

    @Test
    fun `submitHelper should handle download file request`() = runTest {
        // Given
        val request = DownloadFileRequest(
            requestType = "downloadFile",
        )
        val logId = "download-test"

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = this@SubmitHelperTest.processor
            override val subProcessor: IsBizSubProcessor = this@SubmitHelperTest.subProcessor
            override val settings: IsCorSettings = this@SubmitHelperTest.settings
        }

        // Mock repository
        Mockito.`when`(settings.contextRepository).thenReturn(null)

        // Mock processor
        Mockito.`when`(processor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
        }

        Mockito.doNothing().`when`(subProcessor).exec(any())

        // When
        val result: ErrorResponse = appSettings.submitHelper(
            request = request,
            clazz = SubmitHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("error", result.responseType)
        assertEquals(ResponseResult.ERROR, result.result)
    }
}