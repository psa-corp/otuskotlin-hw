package net.otuskotlin.ingredientscan.tests.e2e.be

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseWiremockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class AnalysisApiWiremockTest : BaseWiremockTest() {

    private val log = LoggerFactory.getLogger(AnalysisApiWiremockTest::class.java)

    @Test
    fun `T-003 - analysis get - success`() {
        // Given
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis_123"
        )

        val requestBody = readRequest(request)
        log.info("Request body: $requestBody")

        // When
        val response = executePost(
            path = "/v1/analysis/get",
            body = requestBody
        )

        assertThat(response.code).isEqualTo(200)

        val responseBody: AnalysisGetResponse = readResponse(response)
        log.info("Parsed response: $responseBody")

        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.analysis).isNotNull
        assertThat(responseBody.analysis?.rating).isBetween(1.0, 5.0)
        assertThat(responseBody.analysis?.color?.toString()?.lowercase()).isIn(
            "dark_red", "red", "orange", "yellow",
            "light_yellow", "light_green", "green", "dark_green"
        )

        assertThat(responseBody.analysis?.problematicComponent).isNotNull
        assertThat(responseBody.analysis?.safeComponent).isNotNull
    }
}