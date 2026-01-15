package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseRestTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class CompositionApiRestTest : BaseRestTest() {

    @Test
    fun `composition create manual returns successful response`() {
        // Arrange
        log.info("Starting composition create manual test")

        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                text = "молоко, сахар, консервант E202",
                type = ScanType.MANUAL
            )
        )

        val requestBody = readRequest(request)
        log.info("Serialized request: $requestBody")

        // Act
        log.info("Sending POST request to /v1/composition/create/manual")
        val response = executePost(
            path = "/v1/composition/create/manual",
            body = requestBody
        )

        // Assert
        log.info("Asserting response status is 200")
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionCreateByManualResponse = readResponse(response)

        log.info("Asserting response contains success result")
        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.contextId).isNotEmpty()
        Assertions.assertThat(responseBody.contextId).isNotBlank()

        log.info("✅ Composition create manual test completed successfully")
    }

    @Test
    fun `composition get by id returns successful response`() {
        // Arrange
        log.info("Starting composition get by id test")

        val compositionId = "comp-test-456"

        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = compositionId
        )

        val requestBody = readRequest(request)
        log.info("Serialized request: $requestBody")

        // Act
        log.info("Sending POST request to /v1/composition/get")
        val response = executePost(
            path = "/v1/composition/get",
            body = requestBody
        )

        // Assert
        log.info("Asserting response status is 200")
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionGetResponse = readResponse(response)

        log.info("Asserting response contains success result and correct composition")
        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.composition).isNotNull
        Assertions.assertThat(responseBody.composition?.id).isEqualTo(compositionId)
        Assertions.assertThat(responseBody.composition?.text).contains("молоко")

        log.info("✅ Composition get by id test completed successfully")
    }
}