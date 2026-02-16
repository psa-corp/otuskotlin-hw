package net.otuskotlin.ingredientscan.scanner.utils

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.mappers.v1.external.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransport
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

import net.otuskotlin.ingredientscan.api.v1.internal.apiV1InternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponse
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.mappers.v1.internal.fromTransportInternal
import net.otuskotlin.ingredientscan.mappers.v1.internal.toTransportInternal

open class ControllerUtil {
    companion object {
        val CONTEXT_ID = IsContextId("context-fad8a9a5")

        fun serializeRequest(request: IRequest): String = apiV1ExternalRequestSerialize(request)

        fun createStubContext(request: IRequest, contextId: IsContextId, photo: MutableList<String>?): IsContext {
            val context = IsContext()

           if(photo == null) {
               context.fromTransport(request)
           } else {
               context.fromTransport(request, photo)
           }
            when (context.command){
                IsCommand.ANALYSIS_CREATE,
                IsCommand.ANALYSIS_GET,
                IsCommand.ANALYSIS_REGENERATE -> context.analysisResponse = STUB_ANALYSIS
                IsCommand.COMPOSITION_GET -> context.compositionResponse = STUB_COMPOSITION
                IsCommand.COMPOSITION_CONTEXT_GET,
                IsCommand.COMPOSITION_CREATE_MANUAL,
                IsCommand.COMPOSITION_CREATE_PHOTOS ->{
                    context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
                    context.id = STUB_COMPOSITION_CONTEXT_FINISHING.id
                }
                else -> {}
            }

            if (IsContextId.NONE != contextId) {
                context.id = contextId
            }

            context.state = IsState.FINISHING
            return context
        }

        fun testStub(client: WebTestClient, request: IRequest, url: String) {
            testStub(client, request, url, IsContextId.NONE)
        }

        fun testStub(client: WebTestClient, request: IRequest, url: String, contextId: IsContextId) {
            val response = createStubContext(request, contextId, null).toTransport()
            client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(serializeRequest(request)))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(response.javaClass)
                .isEqualTo(response)
        }

        fun testDownload(
            client: WebTestClient,
            fileNames: List<String>,
            expectedStatus: HttpStatus = HttpStatus.OK,
            expectedContentType: String = "application/zip",
            expectedContentDisposition: String = "attachment; filename=\"images.zip\"",
            additionalAssertions: (WebTestClient.ResponseSpec) -> Unit = {}
        ) {
            val exchange = client.get()
                .uri { builder ->
                    builder.path("/v1/download/files")
                        .queryParam("fileName", fileNames)
                        .build()
                }
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)

            if (expectedStatus.is2xxSuccessful) {
                exchange.expectHeader().contentType(expectedContentType)
                exchange.expectHeader().value(HttpHeaders.CONTENT_DISPOSITION) { value ->
                    assertTrue(value.contains(expectedContentDisposition))
                }
            } else {
                exchange.expectHeader().contentType(MediaType.APPLICATION_JSON)
            }

            additionalAssertions(exchange)
        }

        fun createInternalStubContext(request: InternalRequest, contextId: IsContextId = IsContextId.NONE): InternalContext {

            val context = InternalContext().apply {
                fromTransportInternal(request)
            }

            when (context.command) {
                InternalCommand.ANALYSIS_FIND,
                InternalCommand.ANALYSIS_SAVE -> {
                    context.analysisResponse = STUB_ANALYSIS
                }
                InternalCommand.COMPOSITION_FIND,
                InternalCommand.COMPOSITION_SAVE -> {
                    context.compositionResponse = STUB_COMPOSITION
                }
                else -> {}
            }

            if (contextId != IsContextId.NONE) {
                context.id = contextId
            }

            context.state = IsState.FINISHING
            return context
        }

        fun testInternalStub(
            client: WebTestClient,
            request: InternalRequest,
            url: String,
            contextId: IsContextId = IsContextId.NONE,
            headers: (WebTestClient.RequestBodySpec) -> WebTestClient.RequestBodySpec = { it }
        ) {

            val expectedResponse = createInternalStubContext(request, contextId).toTransportInternal()

            @Suppress("UNCHECKED_CAST")
            val responseClass = expectedResponse::class.java as Class<InternalResponse>

            client.post()
                .uri(url)
                .let { headers(it) }
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(apiV1InternalRequestSerialize(request)))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(responseClass)
                .isEqualTo(expectedResponse)
        }
    }
}