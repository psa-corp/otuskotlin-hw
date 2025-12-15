package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanManualDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanPhotosDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanType
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.web.multipart.MultipartFile
import kotlin.collections.isEmpty

@WebMvcTest(
    value = [CompositionController::class]
)
class CompositionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var s3CloudService: S3CloudService

    @Test
    fun `compositionCreateByPhotos single photo success`() {
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val scanDataPart = readRequest(request)
        val scanPart = MockMultipartFile(
            "scan",
            "scan.json",
            MediaType.APPLICATION_JSON_VALUE,
            scanDataPart.toByteArray()
        )


        val mockFile = MockMultipartFile(
            "photos",
            "photo1.jpg",
            "image/jpeg",
            "photo data".toByteArray()
        )

        whenever(s3CloudService.uploadFiles(any(), any(), any()))
            .thenReturn(mutableListOf("photo1.jpg"))

        mockMvc.multipart("/composition/create/photos") {
            file(mockFile)
            file(scanPart)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `compositionCreateByPhotos multiple photos success`() {
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val scanDataPart = readRequest(request)
        val scanPart = MockMultipartFile(
            "scan",
            "scan.json",
            MediaType.APPLICATION_JSON_VALUE,
            scanDataPart.toByteArray()
        )

        val file1 = MockMultipartFile(
            "photos",
            "photo1.jpg",
            "image/jpeg",
            "photo1 data".toByteArray()
        )
        val file2 = MockMultipartFile(
            "photos",
            "photo2.jpg",
            "image/jpeg",
            "photo2 data".toByteArray()
        )

        whenever(s3CloudService.uploadFiles(any(), any(), any()))
            .thenReturn(mutableListOf("photo1.jpg", "photo2.jpg"))

        mockMvc.multipart("/composition/create/photos") {
            file(file1)
            file(file2)
            file(scanPart)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `compositionCreateByPhotos too many files`() {
        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val scanDataPart = readRequest(request)
        val scanPart = MockMultipartFile(
            "scan",
            "scan.json",
            MediaType.APPLICATION_JSON_VALUE,
            scanDataPart.toByteArray()
        )

        val files = Array(10) { index ->
            MockMultipartFile(
                "photos",
                "photo$index.jpg",
                "image/jpeg",
                "data$index".toByteArray()
            )
        }

        doAnswer { invocation ->
            val ctx = invocation.getArgument<IsContext>(0)
            val files = invocation.getArgument<Array<MultipartFile>>(1)
            // Проверка в сервисе
            if (files.size > 3) {
                ctx.errors.add(IsError(
                    code = "TOO_MANY_FILES",
                    group = "s3",
                    field = "",
                    message = "Too many files: max 3 allowed"
                ))
            }
            mutableListOf<String>()
        }.whenever(s3CloudService).uploadFiles(
            argThat { context -> context is IsContext },
            argThat { files -> files.isNotEmpty() },
            isNull()
        )

        mockMvc.multipart("/composition/create/photos") {
            files.forEach { file(it) }
            file(scanPart)
        }.andExpect {
            status { isBadRequest() }
        }

    }

    @Test
    fun `compositionCreateByPhotos no files`() {

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val scanDataPart = readRequest(request)
        val scanPart = MockMultipartFile(
            "scan",
            "scan.json",
            MediaType.APPLICATION_JSON_VALUE,
            scanDataPart.toByteArray()
        )

        val files =  mutableListOf<MockMultipartFile>()

        doAnswer { invocation ->
            val ctx = invocation.getArgument<IsContext>(0)
            val files = invocation.getArgument<Array<MultipartFile>>(1)
            // Проверка в сервисе
            if (files.isEmpty()) {
                ctx.errors.add(IsError(
                    code = "NO_FILES",
                    group = "s3",
                    field = "",
                    message = "No files provided"
                ))
            }
            mutableListOf<String>()
        }.whenever(s3CloudService).uploadFiles(
            argThat { context -> context is IsContext },
            argThat { files -> files.isNotEmpty() },
            isNull()
        )

        mockMvc.multipart("/composition/create/photos") {
            files.forEach { file(it) }
            file(scanPart)
        }.andExpect {
            status { isBadRequest() }
        }


    }

    @Test
    fun `compositionCreateByManual success`() {
        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                type = ScanType.MANUAL,
                text = "молоко, сахар, консервант E202"
            )
        )

        val requestBody = readRequest(request)

        mockMvc.post("/composition/create/manual") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `compositionGet success`() {
        // Act & Assert

        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = "composition-123"
        )

        val requestBody = readRequest(request)

        mockMvc.post("/composition/get") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    fun readRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }
}