package net.otuskotlin.ingredientscan.app.content

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsContentProvider
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.common.logging.IsLoggerProvider
import net.otuskotlin.ingredientscan.core.common.logging.IsLogWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

import kotlin.reflect.KClass

class DownloadHelperTest {

    @Test
    fun `downloadHelper should return flux of data buffers when content provider exists`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockContentProvider = Mockito.mock(IsContentProvider::class.java)
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)

        // Create test DataBuffer
        val bufferFactory = DefaultDataBufferFactory()
        val buffer = bufferFactory.allocateBuffer()
        buffer.write(byteArrayOf(1, 2, 3, 4, 5))

        val expectedFlux = Flux.just(buffer)

        // Setup mocks
        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(mockContentProvider)
        Mockito.`when`(mockSettings.contextRepository).thenReturn(mockContextRepository)
        Mockito.`when`(mockContentProvider.download(any())).thenReturn(expectedFlux)
        Mockito.doNothing().`when`(mockContextRepository).save(any())

        // Create appSettings implementation
        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = Mockito.mock(IsBizProcessor::class.java)
            override val subProcessor: IsBizSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)
            override val settings: IsCorSettings = mockSettings
        }

        val fileNames = listOf("file1.txt", "file2.jpg")
        val logId = "download-test"

        // When
        val resultFlux = appSettings.downloadHelper(
            fileNames = fileNames,
            clazz = DownloadHelperTest::class,
            logId = logId
        )

        // Then
        StepVerifier.create(resultFlux)
            .expectNext(buffer)
            .verifyComplete()

        // Verify interactions
        Mockito.verify(mockContentProvider).download(any())
        Mockito.verify(mockContextRepository).save(any())
    }

    @Test
    fun `downloadHelper should throw IllegalStateException when content provider is null`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)

        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(null)

        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = Mockito.mock(IsBizProcessor::class.java)
            override val subProcessor: IsBizSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)
            override val settings: IsCorSettings = mockSettings
        }

        val fileNames = listOf("file1.txt")
        val logId = "download-null-test"

        // When / Then
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest {
                appSettings.downloadHelper(fileNames, DownloadHelperTest::class, logId)
            }
        }

        assertEquals("Content provider is not configured", exception.message)
    }

    @Test
    fun `downloadHelper should propagate exception when content provider download fails`() = runTest {
        // Given
        val mockSettings = Mockito.mock(IsCorSettings::class.java)
        val mockLoggerProvider = Mockito.mock(IsLoggerProvider::class.java)
        val mockContentProvider = Mockito.mock(IsContentProvider::class.java)
        val mockContextRepository = Mockito.mock(IsContextRepository::class.java)

        val mockLogWrapper = Mockito.mock(IsLogWrapper::class.java)
        Mockito.`when`(mockSettings.loggerProvider).thenReturn(mockLoggerProvider)
        Mockito.`when`(mockLoggerProvider.logger(any<KClass<*>>())).thenReturn(mockLogWrapper)

        Mockito.`when`(mockSettings.contentProvider).thenReturn(mockContentProvider)
        Mockito.`when`(mockSettings.contextRepository).thenReturn(mockContextRepository)

        val exception = RuntimeException("S3 download failed")
        Mockito.`when`(mockContentProvider.download(any())).thenThrow(exception)
        Mockito.doNothing().`when`(mockContextRepository).save(any())

        val appSettings = object : IsAppSettings {
            override val processor: IsBizProcessor = Mockito.mock(IsBizProcessor::class.java)
            override val subProcessor: IsBizSubProcessor = Mockito.mock(IsBizSubProcessor::class.java)
            override val settings: IsCorSettings = mockSettings
        }

        val fileNames = listOf("file1.txt")
        val logId = "download-error-test"

        // When / Then
        val thrownException = assertThrows(RuntimeException::class.java) {
            runTest {
                appSettings.downloadHelper(fileNames, DownloadHelperTest::class, logId)
            }
        }

        assertEquals("S3 download failed", thrownException.message)

        // Verify context was saved despite the error
        Mockito.verify(mockContextRepository).save(any())
    }
}