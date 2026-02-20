package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.scanner.filters.InternalApiFilter
import net.otuskotlin.ingredientscan.scanner.services.await.ContextAwaitService
import net.otuskotlin.ingredientscan.scanner.services.biz.BizKafkaSender
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testDownload
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets

@WebFluxTest(
    controllers = [DownloadController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [InternalApiFilter::class]
    )]
)
class DownloadControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bizService: BizService

    @MockitoBean
    private lateinit var kafkaSender: BizKafkaSender

    @MockitoBean
    private lateinit var compositionRepository: IsCompositionRepository

    @MockitoBean
    private lateinit var contextRepository: IsContextRepository

    @MockitoBean
    private lateinit var analysisRepository: IsAnalysisRepository

    @MockitoBean
    private lateinit var s3CloudService: S3CloudService

    @MockitoBean
    private lateinit var contextAwaitService: ContextAwaitService

    private val dataBufferFactory: DataBufferFactory = DefaultDataBufferFactory()

    @Test
    fun `downloadAll returns zip file with correct headers and content`(): Unit = runBlocking {
        // Arrange
        val fileNames = listOf("photo1.jpg", "photo2.jpg")
        val zipContent = "mock zip content".toByteArray(StandardCharsets.UTF_8)
        val zipDataBuffer = dataBufferFactory.wrap(zipContent)

        doReturn(Flux.just(zipDataBuffer))
            .`when`(bizService)
            .execute(eq(fileNames), eq("DownloadFile"))

        // Act & Assert
        testDownload(
            client = webTestClient,
            fileNames = fileNames,
            expectedStatus = HttpStatus.OK,
            expectedContentType = "application/zip",
            expectedContentDisposition = "attachment; filename=\"images.zip\""
        )
    }

    @Test
    fun `downloadAll returns error when file not found`(): Unit = runBlocking {
        // Arrange
        val fileNames = listOf("nonexistent.jpg")

        doThrow(RuntimeException("File not found"))
            .`when`(bizService)
            .execute(eq(fileNames), eq("DownloadFile"))

        // Act & Assert
        testDownload(
            client = webTestClient,
            fileNames = fileNames,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            expectedContentType = "application/zip"
        )
    }

    @Test
    fun `downloadAll with special characters in filenames works`(): Unit = runBlocking {
        // Arrange
        val fileNames = listOf("test file (1).jpg", "фото.jpg")
        val zipContent = "zip content".toByteArray(StandardCharsets.UTF_8)
        val zipDataBuffer = dataBufferFactory.wrap(zipContent)

        doReturn(Flux.just(zipDataBuffer))
            .`when`(bizService)
            .execute(eq(fileNames), eq("DownloadFile"))

        // Act & Assert
        testDownload(
            client = webTestClient,
            fileNames = fileNames,
            expectedStatus = HttpStatus.OK,
            expectedContentType = "application/zip",
            expectedContentDisposition = "attachment; filename=\"images.zip\""
        )
    }
}