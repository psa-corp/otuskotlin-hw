package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart

import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.kotlin.any

@WebFluxTest(MediaController::class)
class MediaControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bizService: BizService

    //конфликт корутин и webflugs тест буду проверять на интеграционных тестах

//    @Test
//    fun `compositionCreateByPhotos returns successful response`(): Unit = runBlocking {
//    // Arrange
//        val request = CompositionCreateByPhotosRequest(
//            requestType = "compositionCreateByPhotos",
//            scan = ScanPhotosDto(type = ScanType.PHOTO)
//        )
//
//        val response = CompositionCreateByPhotosResponse(
//            responseType = "compositionCreateByPhotos",
//            result = ResponseResult.SUCCESS,
//            contextId = "context_5678"
//        )
//
//        doAnswer { response }
//            .`when`(bizService)
//            .execute(
//                any<CompositionCreateByPhotosRequest>(),
//                any()
//            )
//
//        val builder = MultipartBodyBuilder().apply {
//            part("photos", "photo1 data".toByteArray())
//                .filename("photo1.jpg")
//                .contentType(MediaType.IMAGE_JPEG)
//
//            part("scan",  request)
//                .contentType(MediaType.APPLICATION_JSON)
//        }
//
//        // Act & Assert
//        webTestClient.post()
//            .uri("/media/composition/create/photos")
//            .contentType(MediaType.MULTIPART_FORM_DATA)
//            .body(BodyInserters.fromMultipartData(builder.build()))
//            .exchange()
//            .expectStatus().isOk
//            .expectHeader().contentType(MediaType.APPLICATION_JSON)
//            .expectBody<CompositionCreateByPhotosResponse>()
//            .value {
//                assert(it.result == ResponseResult.SUCCESS)
//                assert(it.contextId == "context_5678")
//            }
//    }
}