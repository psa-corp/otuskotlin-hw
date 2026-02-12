package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternalTransport
import net.otuskotlin.ingredientscan.mappers.v1.internal.toTransportInternal
import net.otuskotlin.ingredientscan.scanner.configs.InternalSecurityProperties
import net.otuskotlin.ingredientscan.scanner.services.biz.BizInternalService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.createInternalStubContext
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testInternalStub
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [InternalController::class])
class InternalControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bizInternalService: BizInternalService

    @MockitoBean
    private lateinit var compositionRepository: IsCompositionRepository

    @MockitoBean
    private lateinit var analysisRepository: IsAnalysisRepository

    @MockitoBean
    private lateinit var securityProps: InternalSecurityProperties

    @BeforeEach
    fun setUp() {
        doReturn("/v1/internal").`when`(securityProps).prefix
        doReturn("X-Internal-Secret").`when`(securityProps).header
        doReturn("default-secret").`when`(securityProps).token
    }

    private fun withAuth(): (WebTestClient.RequestBodySpec) -> WebTestClient.RequestBodySpec = {
        it.header(securityProps.header, securityProps.token)
    }

    @Test
    fun `internalAnalysisFind returns successful response`(): Unit = runBlocking {
        val request = InternalAnalysisFindRequest(
            requestType = "internalAnalysisFind",
            compositionId = "composition-123"
        )

        val expectedResponse = createInternalStubContext(request).toTransportInternal()

        doReturn(expectedResponse)
            .`when`(bizInternalService)
            .execute<InternalAnalysisFindResponse>(
                request = any<InternalAnalysisFindRequest>(),
                operation = eq("InternalAnalysisFind")
            )

        testInternalStub(
            client = webTestClient,
            request = request,
            url = "/v1/internal/analysis/find",
            headers = withAuth()
        )

        verify(bizInternalService).execute<InternalAnalysisFindResponse>(
            request = any<InternalAnalysisFindRequest>(),
            operation = eq("InternalAnalysisFind")
        )
    }

    @Test
    fun `internalAnalysisSave returns successful response`(): Unit = runBlocking {
        val request = InternalAnalysisSaveRequest(
            requestType = "internalAnalysisSave",
            analysis = STUB_ANALYSIS.toInternalTransport()!!
        )

        val expectedResponse = createInternalStubContext(request).toTransportInternal()

        doReturn(expectedResponse)
            .`when`(bizInternalService)
            .execute<InternalAnalysisSaveResponse>(
                request = any<InternalAnalysisSaveRequest>(),
                operation = eq("InternalAnalysisSave")
            )

        testInternalStub(
            client = webTestClient,
            request = request,
            url = "/v1/internal/analysis/save",
            headers = withAuth()
        )

        verify(bizInternalService).execute<InternalAnalysisSaveResponse>(
            request = any<InternalAnalysisSaveRequest>(),
            operation = eq("InternalAnalysisSave")
        )
    }

    @Test
    fun `internalCompositionFind returns successful response`(): Unit = runBlocking {
        val request = InternalCompositionFindRequest(
            requestType = "internalCompositionFind",
            text = "Соль, сахар"
        )

        val expectedResponse = createInternalStubContext(request).toTransportInternal()

        doReturn(expectedResponse)
            .`when`(bizInternalService)
            .execute<InternalCompositionFindResponse>(
                request = any<InternalCompositionFindRequest>(),
                operation = eq("InternalCompositionFind")
            )

        testInternalStub(
            client = webTestClient,
            request = request,
            url = "/v1/internal/composition/find",
            headers = withAuth()
        )

        verify(bizInternalService).execute<InternalCompositionFindResponse>(
            request = any<InternalCompositionFindRequest>(),
            operation = eq("InternalCompositionFind")
        )
    }

    @Test
    fun `internalCompositionSave returns successful response`(): Unit = runBlocking {
        val request = InternalCompositionSaveRequest(
            requestType = "internalCompositionSave",
            composition = STUB_COMPOSITION.toInternalTransport()!!
        )

        val expectedResponse = createInternalStubContext(request).toTransportInternal()

        doReturn(expectedResponse)
            .`when`(bizInternalService)
            .execute<InternalCompositionSaveResponse>(
                request = any<InternalCompositionSaveRequest>(),
                operation = eq("InternalCompositionSave")
            )

        testInternalStub(
            client = webTestClient,
            request = request,
            url = "/v1/internal/composition/save",
            headers = withAuth()
        )

        verify(bizInternalService).execute<InternalCompositionSaveResponse>(
            request = any<InternalCompositionSaveRequest>(),
            operation = eq("InternalCompositionSave")
        )
    }

    @Test
    fun `internalAnalysisFind returns error when not found`(): Unit = runBlocking {
        val request = InternalAnalysisFindRequest(
            requestType = "internalAnalysisFind",
            compositionId = "not-found"
        )

        val errorResponse = InternalAnalysisFindResponse(
            responseType = "internalAnalysisFind",
            result = InternalResponseResult.ERROR,
            errors = listOf(
                InternalError(
                    code = "NOT_FOUND",
                    group = "analysis",
                    field = "compositionId",
                    message = "Analysis not found"
                )
            ),
            analysis = null
        )

        doReturn(errorResponse)
            .`when`(bizInternalService)
            .execute<InternalAnalysisFindResponse>(
                request = any<InternalAnalysisFindRequest>(),
                operation = eq("InternalAnalysisFind")
            )

        webTestClient.post()
            .uri("/v1/internal/analysis/find")
            .header(securityProps.header, securityProps.token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(InternalAnalysisFindResponse::class.java)
            .value { response ->
                assert(response.result == InternalResponseResult.ERROR)
                assert(response.errors?.first()?.code == "NOT_FOUND")
                assert(response.analysis == null)
            }

        verify(bizInternalService).execute<InternalAnalysisFindResponse>(
            request = any<InternalAnalysisFindRequest>(),
            operation = eq("InternalAnalysisFind")
        )
    }
}