package net.otuskotlin.ingredientscan.tests.e2e.be.base

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalResponseDeserialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.tests.e2e.be.TestApplication
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.io.File
import java.time.Duration


@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
    locations = ["classpath:application-test.yaml"],
    properties = [
        "spring.docker.compose.enabled=true",
        "spring.docker.compose.skip.in-tests=false"
    ]
)
open class BaseRestTest {
    protected val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(30))
        .connectTimeout(Duration.ofSeconds(30))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(30))
        .build()

    companion object {
        private val log by lazy { LoggerFactory.getLogger(BaseRestTest::class.java) }

        protected const val APP_PORT = 8081
        protected const val APP_HOST = "localhost"


    }

    @BeforeEach
    fun waitForApplication() {
        log.debug("══════════════════════════════════════════════════════════════")
        log.debug("🚀 Docker Compose будет запущен Spring Boot автоматически")
        log.debug("📄 Файл docker-compose/docker-compose-test.yml существует? ${File("docker-compose/docker-compose-test.yml").exists()}")
        log.debug("⏳ Ожидание запуска приложения (максимум 180 секунд)...")

        waitForAppReady(180)

        log.debug("✅ Application is ready for testing!")
        log.debug("══════════════════════════════════════════════════════════════")
    }

    private fun waitForAppReady(timeoutSeconds: Int) {
        val startTime = System.currentTimeMillis()
        var attempt = 0

        while (true) {
            attempt++
            val healthUrl = "http://$APP_HOST:$APP_PORT/v1/actuator/health"
            log.debug("🔍 Health check attempt #$attempt: $healthUrl")

            try {
                val response = client.newCall(
                    Request.Builder()
                        .url(healthUrl)
                        .get()
                        .build()
                ).execute()

                if (response.code == 200) {
                    println("✅ Health check PASSED!")
                    response.close()
                    return
                }
                response.close()
            } catch (e: Exception) {
                log.debug("❌ Health check failed: ${e.message}")
            }

            if (System.currentTimeMillis() - startTime > timeoutSeconds * 1000L) {
                throw IllegalStateException("App not started within $timeoutSeconds seconds")
            }

            Thread.sleep(2000)
        }
    }

    protected fun getBaseUrl(): String = "http://$APP_HOST:$APP_PORT"

    protected fun executeGet(path: String): Response {
        val url = "${getBaseUrl()}$path"
        val log = LoggerFactory.getLogger(this::class.java)
        log.debug("📤 GET Request: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return client.newCall(request).execute()
    }

    protected fun executePost(
        path: String,
        body: String,
        contentType: String = "application/json"
    ): Response {
        val url = "${getBaseUrl()}$path"
        val log = LoggerFactory.getLogger(this::class.java)
        log.debug("📤 POST Request: $url")
        log.debug("   Content-Type: $contentType")
        log.debug("   Body: $body")

        val mediaType = contentType.toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType))
            .build()

        return client.newCall(request).execute()
    }

    protected inline fun <reified T> readResponse(response: Response): T {
        val log = LoggerFactory.getLogger(this::class.java)
        val responseBodyText = response.body?.string()
            ?: throw IllegalStateException("Response body is null")

        log.debug("📥 Response (${response.code}): $responseBodyText")

        return apiV1ExternalResponseDeserialize(responseBodyText)
    }

    protected fun readRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }
}
