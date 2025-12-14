package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import software.amazon.awssdk.services.s3.model.HeadObjectResponse

@WebMvcTest(DownloadController::class)
class DownloadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var s3Service: S3CloudService

    @Test
    fun `download file success`() {
        // Arrange
        val fileContent = "test file content"
        val mockResource = ByteArrayResource(fileContent.toByteArray())
        val mockMetadata = HeadObjectResponse.builder()
            .contentType("image/jpeg")
            .contentLength(fileContent.length.toLong())
            .build()

        whenever(s3Service.downloadFileAsResource(any(), any())).thenReturn(mockResource)
        whenever(s3Service.getObjectMetadata(any(), any())).thenReturn(mockMetadata)

        // Act & Assert
        mockMvc.get("/download/file/test.jpg")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.parseMediaType("image/jpeg")) }
            }
    }

    @Test
    fun `download file not found`() {
        // Arrange
        whenever(s3Service.downloadFileAsResource(any(), any())).thenReturn(null)
        whenever(s3Service.getObjectMetadata(any(), any())).thenReturn(null)

        // Act & Assert
        mockMvc.get("/download/file/missing.jpg")
            .andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
    }

    @Test
    fun `download file with special characters`() {
        // Arrange
        val fileName = "test file (1).jpg"
        val fileContent = "content"
        val mockResource = ByteArrayResource(fileContent.toByteArray())
        val mockMetadata = HeadObjectResponse.builder()
            .contentType("image/jpeg")
            .contentLength(fileContent.length.toLong())
            .build()

        whenever(s3Service.downloadFileAsResource(any(), any())).thenReturn(mockResource)
        whenever(s3Service.getObjectMetadata(any(), any())).thenReturn(mockMetadata)

        // Act & Assert
        mockMvc.get("/download/file/$fileName")
            .andExpect {
                status { isOk() }
            }
    }
}