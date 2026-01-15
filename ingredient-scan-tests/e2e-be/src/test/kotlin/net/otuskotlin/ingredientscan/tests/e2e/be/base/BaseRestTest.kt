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
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.Duration

@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
    locations = ["classpath:application-test.yaml"],
    properties = [
        "spring.docker.compose.enabled=false",
        "spring.docker.compose.skip.in-tests=true"
    ]
)
open class BaseRestTest {

    protected val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(30))
        .connectTimeout(Duration.ofSeconds(30))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(30))
        .build()

    protected val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        protected const val APP_PORT = 8081
        protected const val APP_HOST = "localhost"
    }

    protected fun getBaseUrl(): String = "http://$APP_HOST:$APP_PORT"

    protected fun executeGet(path: String): Response {
        val url = "${getBaseUrl()}$path"
        log.info("📤 Sending GET request to: $url")

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
        log.info("📤 Sending POST request to: $url")
        log.debug("Request body: $body")

        val mediaType = contentType.toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType))
            .build()

        return client.newCall(request).execute()
    }

    protected inline fun <reified T> readResponse(response: Response): T {
        val responseBodyText = response.body?.string()
            ?: throw IllegalStateException("Response body is null")

        log.info("📥 Received response with status: ${response.code}")
        log.debug("Response body: $responseBodyText")

        return apiV1ExternalResponseDeserialize(responseBodyText)
    }

    protected fun readRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }
}