package net.otuskotlin.ingredientscan.scanner.controllers

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters

class MediaControllerTest {

    private val bizService: BizService = mockk()
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        val controller = MediaController(bizService)

        webTestClient = WebTestClient.bindToController(controller)
            .configureClient()
            .build()
    }

    @Test
    fun `compositionCreateByPhotos returns successful response`() = runTest {
        val response = CompositionCreateByPhotosResponse(
            responseType = "compositionCreateByPhotos",
            result = ResponseResult.SUCCESS,
            contextId = "context_5678"
        )

        coEvery {
            bizService.execute(any(), any())
        } returns response

        val builder = MultipartBodyBuilder().apply {
            part("photos", "photo".toByteArray())
                .filename("photo1.jpg")
                .contentType(MediaType.IMAGE_JPEG)

            part(
                "scan",
                CompositionCreateByPhotosRequest(
                    requestType = "compositionCreateByPhotos",
                    scan = ScanPhotosDto(type = ScanType.PHOTO)
                )
            ).contentType(MediaType.APPLICATION_JSON)
        }

        // Отправка запроса и проверка ответа
        webTestClient.post()
            .uri("/v1/media/composition/create/photos")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody<CompositionCreateByPhotosResponse>()
            .value {
                assert(it.result == ResponseResult.SUCCESS)
                assert(it.contextId == "context_5678")
            }
    }

}
