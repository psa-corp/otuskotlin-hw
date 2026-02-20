package net.otuskotlin.ingredientscan.app.content

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.logging.IsLoggerProvider
import net.otuskotlin.ingredientscan.core.common.logging.IsLogWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import kotlin.reflect.KClass

class UploadHelperTest {

    @Test
    fun `uploadHelper should process photos upload successfully`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockContentProvider = Mockito.mock(IsContentProvider::class.java)
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)
        val mockProcessor = Mockito.mock(IsBizProcessor::class.java)
        val mockSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)

        // Setup mocks
        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(mockContentProvider)
        Mockito.`when`(mockSettings.contextRepository).thenReturn(mockContextRepository)

        // Mock FilePart
        val mockFilePart = Mockito.mock(FilePart::class.java)
        val photosFlux = Flux.just(mockFilePart, mockFilePart)

        // Mock content provider upload
        val uploadedFileNames = listOf("photo1.jpg", "photo2.jpg")
        Mockito.`when`(mockContentProvider.uploadFlux(any(), any(), any())).thenReturn(uploadedFileNames)

        // Mock processor and subprocessor
        Mockito.`when`(mockProcessor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
            context.compositionResponse = IsComposition(
                id = IsCompositionId("composition-upload123"),
                text = "Uploaded composition",
                createDate = LocalDateTime.now()
            )
        }

        Mockito.doNothing().`when`(mockSubProcessor).exec(any())
        Mockito.doNothing().`when`(mockContextRepository).save(any())

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = mockProcessor
            override val subProcessor: IsBizSubProcessor = mockSubProcessor
            override val settings: IsCorSettings = mockSettings
        }

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO
            )
        )

        val logId = "upload-test"

        // When
        val result = appSettings.uploadHelper<CompositionCreateByPhotosResponse>(
            request = request,
            photos = photosFlux,
            clazz = UploadHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("compositionCreateByPhotos", result.responseType)
        assertEquals(ResponseResult.SUCCESS, result.result)

        // Verify interactions
        Mockito.verify(mockContentProvider).uploadFlux(any(), any(), any())
        Mockito.verify(mockProcessor).exec(any())
        Mockito.verify(mockSubProcessor).exec(any())
        Mockito.verify(mockContextRepository).save(any())
    }

    @Test
    fun `uploadHelper should throw IllegalStateException when content provider is null`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockFilePart = Mockito.mock(FilePart::class.java)
        val photosFlux = Flux.just(mockFilePart)

        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(null)

        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = Mockito.mock(IsBizProcessor::class.java)
            override val subProcessor: IsBizSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)
            override val settings: IsCorSettings = mockSettings
        }

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO
            )
        )

        val logId = "upload-null-test"

        // When / Then
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest {
                appSettings.uploadHelper<CompositionCreateByPhotosResponse>(
                    request = request,
                    photos = photosFlux,
                    clazz = UploadHelperTest::class,
                    logId = logId
                )
            }
        }

        assertEquals("Content provider is not configured", exception.message)
    }

    @Test
    fun `uploadHelper should handle processor exception and return error response`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockContentProvider = Mockito.mock(IsContentProvider::class.java)
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)
        val mockProcessor = Mockito.mock(IsBizProcessor::class.java)
        val mockSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)

        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(mockContentProvider)
        Mockito.`when`(mockSettings.contextRepository).thenReturn(mockContextRepository)

        val mockFilePart = Mockito.mock(FilePart::class.java)
        val photosFlux = Flux.just(mockFilePart)

        val uploadedFileNames = listOf("photo1.jpg")
        Mockito.`when`(mockContentProvider.uploadFlux(any(), any(), any())).thenReturn(uploadedFileNames)

        val exception = RuntimeException("Processor failed")
        Mockito.doThrow(exception).`when`(mockProcessor).exec(any())

        Mockito.doNothing().`when`(mockContextRepository).save(any())

        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = mockProcessor
            override val subProcessor: IsBizSubProcessor = mockSubProcessor
            override val settings: IsCorSettings = mockSettings
        }

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO
            )
        )

        val logId = "upload-error-test"

        // When
        val result = appSettings.uploadHelper<CompositionCreateByPhotosResponse>(
            request = request,
            photos = photosFlux,
            clazz = UploadHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)
        assertEquals("compositionCreateByPhotos", result.responseType)
        assertEquals(ResponseResult.ERROR, result.result)
        assertNotNull(result.errors)
        assertTrue(result.errors?.isNotEmpty() == true)

        // Verify subProcessor was NOT called due to error
        Mockito.verify(mockSubProcessor, Mockito.never()).exec(any())
        Mockito.verify(mockContextRepository).save(any())
    }

    @Test
    fun `uploadHelper should not call subProcessor when context has errors`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockContentProvider = Mockito.mock(IsContentProvider::class.java)
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)
        val mockProcessor = Mockito.mock(IsBizProcessor::class.java)
        val mockSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)

        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(mockContentProvider)
        Mockito.`when`(mockSettings.contextRepository).thenReturn(mockContextRepository)

        val mockFilePart = Mockito.mock(FilePart::class.java)
        val photosFlux = Flux.just(mockFilePart)

        val uploadedFileNames = listOf("photo1.jpg")
        Mockito.`when`(mockContentProvider.uploadFlux(any(), any(), any())).thenReturn(uploadedFileNames)

        // Mock processor to add errors but not throw exception
        Mockito.`when`(mockProcessor.exec(any())).thenAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            context.state = IsState.FINISHING
            context.errors.add(
                IsError(
                    code = "VALIDATION_ERROR",
                    message = "Invalid photo format"
                )
            )
        }

        Mockito.doNothing().`when`(mockContextRepository).save(any())

        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = mockProcessor
            override val subProcessor: IsBizSubProcessor = mockSubProcessor
            override val settings: IsCorSettings = mockSettings
        }

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO
            )
        )

        val logId = "upload-validation-test"

        // When
        val result = appSettings.uploadHelper<CompositionCreateByPhotosResponse>(
            request = request,
            photos = photosFlux,
            clazz = UploadHelperTest::class,
            logId = logId
        )

        // Then
        assertNotNull(result)

        // Verify subProcessor was NOT called because errors exist
        Mockito.verify(mockProcessor).exec(any())
        Mockito.verify(mockSubProcessor, Mockito.never()).exec(any())
    }
}