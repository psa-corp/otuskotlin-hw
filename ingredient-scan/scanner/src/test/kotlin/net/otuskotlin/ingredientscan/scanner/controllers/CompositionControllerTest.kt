package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.testStub
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(CompositionController::class)
class CompositionControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var s3CloudService: S3CloudService

    @MockitoBean
    private lateinit var bizService: BizService

    @Test
    fun `compositionCreateByManual returns successful response`() = runTest {
        // Arrange
        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                type = ScanType.MANUAL,
                text = "молоко, сахар, консервант E202"
            )
        )

        val response = CompositionCreateByManualResponse(
            responseType = "compositionCreateByManual",
            result = ResponseResult.SUCCESS,
            contextId = "context_5678"
        )

        whenever(bizService.execute(any()))
            .thenReturn(response)

        // Act & Assert
        testStub(webTestClient, request, "/composition/create/manual")
    }

    @Test
    fun `compositionGet returns successful response`() = runTest {
        // Arrange
        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = "composition-123"
        )

        val response = CompositionGetResponse(
            responseType = "compositionGet",
            result = ResponseResult.SUCCESS,
            composition = STUB_COMPOSITION.toTransport()
        )

        whenever(bizService.execute(any()))
            .thenReturn(response)

        // Act & Assert
        testStub(webTestClient, request, "/composition/get")
    }

    @Test
    fun `compositionContextGet returns successful response`() = runTest {
        // Arrange
        val request = CompositionContextGetRequest(
            requestType = "compositionContextGet",
            contextId = "context-123"
        )
        val response = CompositionContextGetResponse(
            responseType = "compositionContextGet",
            result = ResponseResult.SUCCESS,
            context = STUB_COMPOSITION_CONTEXT_FINISHING.toTransport()
        )

        whenever(bizService.execute(any()))
            .thenReturn(response)

        // Act & Assert
        testStub(webTestClient, request, "/composition/context/get")
    }
}