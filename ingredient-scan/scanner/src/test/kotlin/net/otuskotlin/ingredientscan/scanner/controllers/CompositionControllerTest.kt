package net.otuskotlin.ingredientscan.scanner.controllers

import kotlinx.coroutines.runBlocking
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransport
import net.otuskotlin.ingredientscan.scanner.filters.InternalApiFilter
import net.otuskotlin.ingredientscan.scanner.services.await.ContextAwaitService
import net.otuskotlin.ingredientscan.scanner.services.biz.BizKafkaSender
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.CONTEXT_ID
import net.otuskotlin.ingredientscan.scanner.utils.ControllerUtil.Companion.createStubContext
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
    controllers = [CompositionController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [InternalApiFilter::class]
    )]
)
class CompositionControllerTest {

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
    fun `compositionCreateByManual returns successful response`(): Unit = runBlocking {
        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                type = ScanType.MANUAL,
                text = "молоко, сахар, консервант E202"
            )
        )

        val expectedResponse = createStubContext(request, IsContextId.NONE, null).toTransport()

        doReturn(expectedResponse)
            .`when`(bizService)
            .execute<CompositionCreateByManualResponse>(
                request = any<CompositionCreateByManualRequest>(),
                operation = eq("CompositionCreateByManual")
            )

        testStub(webTestClient, request, "/v1/composition/create/manual")
        verify(bizService).execute<CompositionCreateByManualResponse>(
            request = any<CompositionCreateByManualRequest>(),
            operation = eq("CompositionCreateByManual")
        )
    }

    @Test
    fun `compositionGet returns successful response`(): Unit = runBlocking {
        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = "composition-123"
        )

        val expectedResponse = createStubContext(request, CONTEXT_ID, null).toTransport()

        doReturn(expectedResponse)
            .`when`(bizService)
            .execute<CompositionGetResponse>(
                request = any<CompositionGetRequest>(),
                operation = eq("CompositionGet")
            )

        testStub(webTestClient, request, "/v1/composition/get", CONTEXT_ID)
        verify(bizService).execute<CompositionGetResponse>(
            request = any<CompositionGetRequest>(),
            operation = eq("CompositionGet")
        )
    }

    @Test
    fun `compositionContextGet returns successful response`(): Unit = runBlocking {
        val request = CompositionContextGetRequest(
            requestType = "compositionContextGet",
            contextId = "context-5678"
        )
        val expectedResponse = createStubContext(request, CONTEXT_ID,null).toTransport()

        doReturn(expectedResponse)
            .`when`(bizService)
            .execute<CompositionContextGetResponse>(
                request = any<CompositionContextGetRequest>(),
                operation = eq("CompositionContextGet")
            )

        testStub(webTestClient, request, "/v1/composition/context/get", CONTEXT_ID)
        verify(bizService).execute<CompositionContextGetResponse>(
            request = any<CompositionContextGetRequest>(),
            operation = eq("CompositionContextGet")
        )
    }
}