package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testStub
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateResponse

@WebFluxTest(AnalysisController::class)
class AnalysisControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var bizService: BizService

    @Test
    fun `analysisGet returns successful response`() = runTest {
        // Arrange
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis-test-123"
        )

        val response = AnalysisGetResponse(
            responseType = "analysisGet",
            result = ResponseResult.SUCCESS,
            analysis = STUB_ANALYSIS.toTransport()
        )

        whenever(bizService.execute(any()))
            .thenReturn(response)

        // Act & Assert
        testStub(webTestClient, request, "/v1/analysis/get")
    }

    @Test
    fun `analysisRegenerate returns successful response`() = runTest {
        // Arrange
        val request = AnalysisRegenerateRequest(
            requestType = "analysisRegenerate",
            analysisId = "analysis-test-123"
        )

        val response = AnalysisRegenerateResponse(
            responseType = "analysisRegenerate",
            result = ResponseResult.SUCCESS,
            analysis = STUB_ANALYSIS.toTransport()
        )

        whenever(bizService.execute(any()))
            .thenReturn(response)

        // Act & Assert
        testStub(webTestClient, request, "/v1/analysis/regenerate")
    }

}