package net.otuskotlin.ingredientscan.tests.e2e.be.base

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalResponseDeserialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration

open class BaseWiremockTest {

    companion object {
        private val log = LoggerFactory.getLogger(BaseWiremockTest::class.java)
        private const val SERVICE_NAME = "app-wiremock_1"
        private const val SERVICE_PORT = 8080

        protected var wireMockPort: Int = 8080
        @JvmStatic
        protected lateinit var client: OkHttpClient

        private lateinit var compose: DockerComposeContainer<*>

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            log.info("Starting WireMock...")

            compose = DockerComposeContainer(File("docker-compose/docker-compose-wiremock.yml"))
                .withExposedService(SERVICE_NAME, SERVICE_PORT)
                .waitingFor(SERVICE_NAME, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(30)))

            compose.start()

            wireMockPort = compose.getServicePort(SERVICE_NAME, SERVICE_PORT)

            client = OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .build()

            log.info("✅ WireMock started on port: $wireMockPort")
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            compose.stop()
            log.info("🛑 WireMock stopped")
        }
    }

    protected fun getBaseUrl(): String = "http://localhost:$wireMockPort"

    protected fun executeGet(path: String) = client.newCall(
        Request.Builder()
            .url("${getBaseUrl()}$path")
            .build()
    ).execute()

    protected fun executePost(
        path: String,
        body: String,
        contentType: String = "application/json"
    ): Response {
        val mediaType = contentType.toMediaType()

        return client.newCall(
            Request.Builder()
                .url("${getBaseUrl()}$path")
                .post(body.toRequestBody(mediaType))
                .build()
        ).execute()
    }

    protected inline fun <reified T : IResponse> readResponse(response: Response): T {
        val responseBodyText = response.body?.string()
            ?: throw IllegalStateException("Response body is null")

        return apiV1ExternalResponseDeserialize(responseBodyText)
    }

    protected fun readRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }

}