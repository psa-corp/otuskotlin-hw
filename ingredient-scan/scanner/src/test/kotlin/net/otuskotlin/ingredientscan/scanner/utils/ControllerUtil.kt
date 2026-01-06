package net.otuskotlin.ingredientscan.scanner.utils

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import kotlin.streams.toList

open class ControllerUtil {
    companion object {
        fun serializeRequest(request: IRequest): String = apiV1ExternalRequestSerialize(request)

        fun createStubContext(request: IRequest, photo: MutableList<String>?): IsContext {
            val context = IsContext()

           if(photo == null) {
               context.fromTransport(request)
           } else {
               context.fromTransport(request, photo)
           }
            when (context.command){
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
            context.state = IsState.FINISHING
            return context
        }

        fun testStub(client: WebTestClient, request: IRequest, url: String) {
            val response = createStubContext(request, null).toTransport()
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

        fun testDownload(client: WebTestClient, url: String, fileName: String, negative: Boolean) {
            val exchange: WebTestClient.ResponseSpec = client.get()
                .uri(url, fileName)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()

            if (negative) {
                exchange
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)

            } else {
                exchange
                    .expectStatus().isOk()
                    .expectHeader().contentType("image/jpeg")
                    .expectBody()
                    .returnResult()
            }
        }

    }
}