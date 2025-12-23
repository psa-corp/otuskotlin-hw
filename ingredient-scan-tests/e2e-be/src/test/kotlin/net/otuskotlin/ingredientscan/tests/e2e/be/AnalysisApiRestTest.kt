package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseRestTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@DisplayName("REST API Tests - AnalysisApi")
class AnalysisApiRestTest : BaseRestTest() {

    private val log by lazy { LoggerFactory.getLogger(AnalysisApiRestTest::class.java) }


    @Test
    fun `T-003 - analysis get - success`() {
        // Given
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis-test-123"
        )

        val requestBody = readRequest(request)
        log.info("Request body: $requestBody")

        // When
        val response = executePost(
            path = "/v1/analysis/get",
            body = requestBody
        )

        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: AnalysisGetResponse = readResponse(response)
        log.info("Parsed response: $responseBody")

        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.analysis).isNotNull
        Assertions.assertThat(responseBody.analysis?.rating).isBetween(1.0, 5.0)


        Assertions.assertThat(responseBody.analysis?.color).isEqualTo(IsColor.GREEN.toTransport())
        Assertions.assertThat(responseBody.analysis?.compositionId).isEqualTo("comp-test-456")
        Assertions.assertThat(responseBody.analysis?.problematicComponent).isNotNull
        Assertions.assertThat(responseBody.analysis?.safeComponent).isNotNull
    }
}
