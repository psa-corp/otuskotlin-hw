package net.otuskotlin.ingredientscan.tests.e2e.be.base

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalResponseDeserialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

open class ApiClient(
    private val baseUrl: String,
    private val connectTimeout: Duration = Duration.ofSeconds(30),
    private val requestTimeout: Duration = Duration.ofSeconds(30)
) {
    private val log = LoggerFactory.getLogger(ApiClient::class.java)
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(connectTimeout)
        .build()

    open fun <R : IResponse> post(request: IRequest, path: String): R {
        val url = baseUrl + path
        val requestBody = apiV1ExternalRequestSerialize(request)

        log.info("POST $url")
        log.debug("Request body: $requestBody")

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(requestTimeout)
            .build()

        val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        log.info("Response status: ${response.statusCode()}")
        log.debug("Response body: ${response.body()}")

        require(response.statusCode() == 200) {
            "HTTP error ${response.statusCode()}: ${response.body()}"
        }

        return apiV1ExternalResponseDeserialize(response.body())
    }
}