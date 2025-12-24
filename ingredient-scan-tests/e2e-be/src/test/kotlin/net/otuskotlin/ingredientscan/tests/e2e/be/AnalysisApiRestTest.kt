package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseRestTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AnalysisApiRestTest : BaseRestTest() {

    @Test
    fun `analysis get returns successful response`() {
        // Arrange
        log.info("Starting analysis get test")

        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis-test-123"
        )

        val requestBody = readRequest(request)
        log.info("Serialized request: $requestBody")

        // Act
        log.info("Sending POST request to /v1/analysis/get")
        val response = executePost(
            path = "/v1/analysis/get",
            body = requestBody
        )

        // Assert
        log.info("Asserting response status is 200")
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: AnalysisGetResponse = readResponse(response)
        log.info("Deserialized response: $responseBody")

        log.info("Asserting response contains success result and valid analysis data")
        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.analysis).isNotNull
        Assertions.assertThat(responseBody.analysis?.rating).isBetween(1.0, 5.0)
        Assertions.assertThat(responseBody.analysis?.color).isEqualTo(IsColor.GREEN.toTransport())
        Assertions.assertThat(responseBody.analysis?.compositionId).isEqualTo("comp-test-456")
        Assertions.assertThat(responseBody.analysis?.problematicComponent).isNotNull
        Assertions.assertThat(responseBody.analysis?.safeComponent).isNotNull

        log.info("✅ Analysis get test completed successfully")
    }
}