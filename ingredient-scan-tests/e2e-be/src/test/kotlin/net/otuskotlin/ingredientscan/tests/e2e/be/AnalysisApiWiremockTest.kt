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

    // Временное решение - локальный mapper
    private val localMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())

    @Test
    fun `T-003 - analysis get - success`() {
        // Given
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis_123"
        )

        val requestBody = localMapper.writeValueAsString(request)
        log.info("Request body: $requestBody")

        // When
        val response = executePost(
            path = "/v1/analysis/get",
            body = requestBody
        )

        log.info("Response code: ${response.code}")

        val responseBodyText = response.body?.string()
        log.info("Response body: $responseBodyText")

        assertThat(response.code).isEqualTo(200)
        assertThat(responseBodyText).isNotEmpty()

        val responseBody: AnalysisGetResponse = localMapper.readValue(responseBodyText, AnalysisGetResponse::class.java)
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

//
//    @Test
//    fun `test-01 02 - analysis get - not found`() {
//
//        val request = AnalysisGetRequest(
//            requestType = "analysisGet",
//            analysisId = "analysis_99999"
//        )
//
//        // When
//        val response = executePost(
//            path = "/v1/analysis/get",
//            body = mapper.writeValueAsString(request)
//        )
//
//        // Then
//        assertThat(response.code).isEqualTo(404)
//
//        val responseBody: ErrorResponse = readResponse(response)
//        assertThat(responseBody.result).isEqualTo(ResponseResult.ERROR)
//        assertThat(responseBody.errors).isNotEmpty
//        assertThat(responseBody.errors?.first()?.message).containsIgnoringCase("не найден")
//    }
}