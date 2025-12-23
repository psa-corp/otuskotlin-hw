package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.web.multipart.MultipartFile

@WebMvcTest(CompositionController::class)
class CompositionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var s3CloudService: S3CloudService

    @MockitoBean
    private lateinit var bizService: BizService

    @Test
    fun `compositionCreateByPhotos with single photo returns successful response`() {
        // Arrange
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(type = ScanType.PHOTO)
        )
        val scanDataPart = serializeRequest(request)
        val scanPart = createJsonMultipartFile("scan", "scan.json", scanDataPart)
        val photoPart = createImageMultipartFile("photos", "photo1.jpg", "photo data")

        whenever(s3CloudService.uploadFiles(any(), any(), any()))
            .thenReturn(mutableListOf("photo1.jpg"))
        whenever(bizService.compositionCreateByPhotos(any()))
            .thenReturn(IsContext())

        // Act & Assert
        mockMvc.multipart("/composition/create/photos") {
            file(photoPart)
            file(scanPart)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `compositionCreateByPhotos with multiple photos returns successful response`() {
        // Arrange
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(type = ScanType.PHOTO)
        )
        val scanDataPart = serializeRequest(request)
        val scanPart = createJsonMultipartFile("scan", "scan.json", scanDataPart)
        val photo1 = createImageMultipartFile("photos", "photo1.jpg", "photo1 data")
        val photo2 = createImageMultipartFile("photos", "photo2.jpg", "photo2 data")

        whenever(s3CloudService.uploadFiles(any(), any(), any()))
            .thenReturn(mutableListOf("photo1.jpg", "photo2.jpg"))
        whenever(bizService.compositionCreateByPhotos(any()))
            .thenReturn(IsContext())

        // Act & Assert
        mockMvc.multipart("/composition/create/photos") {
            file(photo1)
            file(photo2)
            file(scanPart)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `compositionCreateByPhotos with no files returns bad request`() {
        // Arrange
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(type = ScanType.PHOTO)
        )
        val scanDataPart = serializeRequest(request)
        val scanPart = createJsonMultipartFile("scan", "scan.json", scanDataPart)

        whenever(bizService.compositionCreateByPhotos(any()))
            .thenReturn(IsContext())

        doAnswer { invocation ->
            val context = invocation.getArgument<IsContext>(0)
            val files = invocation.getArgument<Array<MultipartFile>>(1)
            if (files.isEmpty()) {
                context.errors.add(
                    IsError(
                        code = "NO_FILES",
                        group = "s3",
                        field = "",
                        message = "No files provided"
                    )
                )
            }
            mutableListOf<String>()
        }.whenever(s3CloudService).uploadFiles(
            argThat { context -> context is IsContext },
            argThat { files -> files.isNotEmpty() },
            isNull()
        )

        // Act & Assert
        mockMvc.multipart("/composition/create/photos") {
            file(scanPart)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `compositionCreateByManual returns successful response`() {
        // Arrange
        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                type = ScanType.MANUAL,
                text = "молоко, сахар, консервант E202"
            )
        )
        val requestBody = serializeRequest(request)

        whenever(bizService.compositionCreateByManual(any()))
            .thenReturn(IsContext())

        // Act & Assert
        mockMvc.post("/composition/create/manual") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `compositionGet returns successful response`() {
        // Arrange
        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = "composition-123"
        )
        val requestBody = serializeRequest(request)

        whenever(bizService.compositionGet(any()))
            .thenReturn(IsContext())

        // Act & Assert
        mockMvc.post("/composition/get") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `compositionContextGet returns successful response`() {
        // Arrange
        val request = CompositionContextGetRequest(
            requestType = "compositionContextGet",
            contextId = "context-123"
        )
        val requestBody = serializeRequest(request)

        whenever(bizService.compositionContextGet(any()))
            .thenReturn(IsContext())

        // Act & Assert
        mockMvc.post("/composition/context/get") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    private fun serializeRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }

    private fun createJsonMultipartFile(name: String, filename: String, content: String): MockMultipartFile {
        return MockMultipartFile(name, filename, MediaType.APPLICATION_JSON_VALUE, content.toByteArray())
    }

    private fun createImageMultipartFile(name: String, filename: String, data: String): MockMultipartFile {
        return MockMultipartFile(name, filename, "image/jpeg", data.toByteArray())
    }
}