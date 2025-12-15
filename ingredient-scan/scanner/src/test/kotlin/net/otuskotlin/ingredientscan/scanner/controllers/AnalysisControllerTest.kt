package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalRequestSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisRegenerateRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(
    value = [AnalysisController::class]
)
class AnalysisControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `analysisGet success`() {
        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "analysis-test-123"
        )

        val requestBody = readRequest(request)

        mockMvc.post("/analysis/get") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }
        .andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }


    @Test
    fun `analysisGet returns stub`() {

        val request = AnalysisGetRequest(
            requestType = "analysisGet",
            analysisId = "any-id"
        )

        val requestBody = readRequest(request)


        mockMvc.post("/analysis/get") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `analysisRegenerate success`() {

        val request = AnalysisRegenerateRequest(
            requestType = "analysisRegenerate",
            analysisId = "analysis-test-123"
        )

        val requestBody = readRequest(request)
        mockMvc.post("/analysis/regenerate") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    fun readRequest(request: IRequest): String {
        return apiV1ExternalRequestSerialize(request)
    }
}