package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisCreateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisCreateResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransport
import net.otuskotlin.ingredientscan.scanner.filters.InternalApiFilter
import net.otuskotlin.ingredientscan.scanner.services.await.ContextAwaitService
import net.otuskotlin.ingredientscan.scanner.services.biz.BizKafkaSender
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testStub
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [AnalysisController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [InternalApiFilter::class]
    )]
)
class AnalysisControllerTest {

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
    fun `analysisGet returns successful response`(): Unit = runBlocking {
        // Arrange
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis-test123"
        )

        val response = AnalysisGetResponse(
            responseType = "analysisGet",
            result = ResponseResult.SUCCESS,
            analysis = STUB_ANALYSIS.toTransport()
        )

        doReturn(response)
            .`when`(bizService)
            .execute<AnalysisGetResponse>(
                request = any<AnalysisGetRequest>(),
                operation = eq("AnalysisGet")
            )

        // Act & Assert
        testStub(webTestClient, request, "/v1/analysis/get")
        verify(bizService).execute<AnalysisGetResponse>(any(), eq("AnalysisGet"))
    }

    @Test
    fun `analysisRegenerate returns successful response`(): Unit = runBlocking {
        // Arrange
        val request = AnalysisRegenerateRequest(
            requestType = "analysisRegenerate",
            analysisId = "analysis-test123"
        )

        val response = AnalysisRegenerateResponse(
            responseType = "analysisRegenerate",
            result = ResponseResult.SUCCESS,
            analysis = STUB_ANALYSIS.toTransport()
        )

        doReturn(response)
            .`when`(bizService)
            .execute<AnalysisRegenerateResponse>(
                request = any<AnalysisRegenerateRequest>(),
                operation = eq("AnalysisRegenerate")
            )
        // Act & Assert
        testStub(webTestClient, request, "/v1/analysis/regenerate")
        verify(bizService).execute<AnalysisRegenerateResponse>(any(), eq("AnalysisRegenerate"))
    }

    @Test
    fun `analysisCreate returns successful response`(): Unit = runBlocking {
        val request = AnalysisCreateRequest(
            requestType = "analysisCreate",
            compositionId = "composition-test123"
        )

        val response = AnalysisCreateResponse(
            responseType = "analysisCreate",
            result = ResponseResult.SUCCESS,
            analysis = STUB_ANALYSIS.toTransport()
        )

        doReturn(response)
            .`when`(bizService)
            .execute<AnalysisCreateResponse>(
                request = any<AnalysisCreateRequest>(),
                operation = eq("AnalysisCreate")   // ← исправлено: большая буква
            )

        testStub(webTestClient, request, "/v1/analysis/create")
        verify(bizService).execute<AnalysisCreateResponse>(any(), eq("AnalysisCreate"))
    }
}