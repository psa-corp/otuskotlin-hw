package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseWiremockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AnalysisApiWiremockTest : BaseWiremockTest() {

    @Test
    fun `T-003 - analysis get - success`() {
        // Given
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis_123"
        )

        // When
        val response = executePost(
            path = "/v1/analysis/get",
            body = mapper.writeValueAsString(request)
        )

        // Then
        assertThat(response.code).isEqualTo(200)

        val responseBody: AnalysisGetResponse = readResponse(response)
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.analysis).isNotNull
        assertThat(responseBody.analysis?.rating).isBetween(1.0, 5.0)
        assertThat(responseBody.analysis?.color).isIn(
            "dark_red", "red", "orange", "yellow",
            "light_yellow", "light_green", "green", "dark_green"
        )

        assertThat(responseBody.analysis?.problematicComponent).isNotNull
        assertThat(responseBody.analysis?.safeComponent).isNotNull
    }

    @Test
    fun `test-01 02 - analysis get - not found`() {

        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis_99999"
        )

        // When
        val response = executePost(
            path = "/v1/analysis/get",
            body = mapper.writeValueAsString(request)
        )

        // Then
        assertThat(response.code).isEqualTo(404)

        val responseBody: ErrorResponse = readResponse(response)
        assertThat(responseBody.result).isEqualTo(ResponseResult.ERROR)
        assertThat(responseBody.errors).isNotEmpty
        assertThat(responseBody.errors?.first()?.message).containsIgnoringCase("не найден")
    }
}