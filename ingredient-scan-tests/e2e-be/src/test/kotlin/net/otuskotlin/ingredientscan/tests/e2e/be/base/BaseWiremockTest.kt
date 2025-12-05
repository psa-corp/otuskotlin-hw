package net.otuskotlin.ingredientscan.tests.e2e.be.base

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
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
        protected lateinit var client: OkHttpClient
        protected val mapper = jacksonObjectMapper()

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

    protected fun executePost(path: String, body: String, contentType: String = "application/json"): okhttp3.Response {
        // Мы знаем, что "application/json" и "image/jpeg" валидные MediaType
        val mediaType = when (contentType) {
            "application/json" -> "application/json".toMediaType()
            "image/jpeg" -> "image/jpeg".toMediaType()
            "multipart/form-data" -> "multipart/form-data".toMediaType()
            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }

        return client.newCall(
            Request.Builder()
                .url("${getBaseUrl()}$path")
                .post(body.toRequestBody(mediaType))
                .build()
        ).execute()
    }
}