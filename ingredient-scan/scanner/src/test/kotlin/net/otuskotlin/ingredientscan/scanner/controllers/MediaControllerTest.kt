package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanPhotosDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanType
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.serializeRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart

@WebMvcTest(MediaController::class)
class MediaControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var s3CloudService: S3CloudService

    @MockitoBean
    private lateinit var bizService: BizService

    @Test
    fun `compositionCreateByPhotos returns successful response`() {
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
        mockMvc.multipart("/media/composition/create/photos") {
            file(photo1)
            file(photo2)
            file(scanPart)
        }.andExpect {
            status { isOk() }
        }
    }

    private fun createJsonMultipartFile(name: String, filename: String, content: String): MockMultipartFile {
        return MockMultipartFile(name, filename, MediaType.APPLICATION_JSON_VALUE, content.toByteArray())
    }

    private fun createImageMultipartFile(name: String, filename: String, data: String): MockMultipartFile {
        return MockMultipartFile(name, filename, "image/jpeg", data.toByteArray())
    }
}