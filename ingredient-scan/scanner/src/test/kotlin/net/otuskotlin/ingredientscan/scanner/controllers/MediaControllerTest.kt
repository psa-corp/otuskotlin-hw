package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.scanner.filters.InternalApiFilter
import net.otuskotlin.ingredientscan.scanner.services.await.ContextAwaitService
import net.otuskotlin.ingredientscan.scanner.services.biz.BizKafkaSender
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux

@WebFluxTest(
    controllers = [MediaController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [InternalApiFilter::class]
    )]
)
class MediaControllerTest {

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

    @Test
    fun `compositionCreateByPhotos returns successful response`(): Unit = runBlocking {
        // Arrange
        val expectedResponse = CompositionCreateByPhotosResponse(
            responseType = "compositionCreateByPhotos",
            result = ResponseResult.SUCCESS,
            contextId = "context-5678"
        )

        doReturn(expectedResponse)
            .`when`(bizService)
            .execute<CompositionCreateByPhotosResponse>(
                any<CompositionCreateByPhotosRequest>(),
                any<Flux<FilePart>>(),
                eq("CompositionCreateByPhotos")
            )

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

        // Act & Assert
        webTestClient.post()
            .uri("/v1/media/composition/create/photos")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody<CompositionCreateByPhotosResponse>()
            .value { response ->
                assert(response.result == ResponseResult.SUCCESS)
                assert(response.contextId == "context-5678")
                assert(response.responseType == "compositionCreateByPhotos")
            }

        verify(bizService).execute<CompositionCreateByPhotosResponse>(
            any<CompositionCreateByPhotosRequest>(),
            any<Flux<FilePart>>(),
            eq("CompositionCreateByPhotos")
        )
    }
}