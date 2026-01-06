package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.mappers.v1.toDownloadFileErrorResponse
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.JsonErrorResource
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testDownload
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import org.springframework.http.HttpStatus

@WebFluxTest(DownloadController::class)
class DownloadControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bizService: BizService

    @MockitoBean
    private lateinit var s3Service: S3CloudService

    @MockitoBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockitoBean
    private lateinit var compositionRepository: InMemoryCompositionRepository

    @MockitoBean
    private lateinit var contextRepository: InMemoryContextRepository

    @Test
    fun `download file returns successful response with correct content type`(): Unit = runBlocking {
        // Arrange
        val fileContent = "test file content"
        val fileName = "test.jpg"
        val mockResource = ByteArrayResource(fileContent.toByteArray())
        val mockMetadata = HeadObjectResponse.builder()
            .contentType("image/jpeg")
            .contentLength(fileContent.length.toLong())
            .build()

        whenever(bizService.get(any())).thenReturn(
            ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(mockResource)
        )

        // Act & Assert
        testDownload(webTestClient, "/download/file/{fileName}", fileName, false)
    }

    @Test
    fun `download file returns not found when file does not exist`(): Unit = runBlocking {
        // Arrange
        val fileName = "nonexistent.jpg"
        val context = IsContext()

        context.errors.add(
            IsError(
                code = "FILE_NOT_FOUND",
                group = "s3",
                field = "",
                message = "File not found: $fileName"
            )
        )

        val errorResponse = context.toDownloadFileErrorResponse()
        val jsonResource = JsonErrorResource(errorResponse)

        whenever(bizService.get(any())).thenReturn(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResource)
        )

        // Act & Assert
        testDownload(webTestClient, "/download/file/{fileName}", fileName, true)
    }

    @Test
    fun `download file with special characters in filename returns successful response`(): Unit = runBlocking {
        // Arrange
        val fileName = "test file (1).jpg"
        val fileContent = "content"
        val mockResource = ByteArrayResource(fileContent.toByteArray())
        val mockMetadata = HeadObjectResponse.builder()
            .contentType("image/jpeg")
            .contentLength(fileContent.length.toLong())
            .build()

        whenever(bizService.get(any())).thenReturn(
            ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(mockResource)
        )

        // Act & Assert
        testDownload(webTestClient, "/download/file/{fileName}", fileName, false)
    }
}