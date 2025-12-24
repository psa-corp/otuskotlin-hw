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

    @Test
    fun `composition create photos with multipart upload returns successful response`() {
        // Arrange
        log.info("Starting composition create photos test with multipart upload")

        val testFile = File.createTempFile("test_label", ".jpg")
        testFile.writeBytes(ByteArray(1024))
        log.info("Created temporary test file: ${testFile.name}")

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val requestDataPart = readRequest(request)
        log.info("Serialized request data: $requestDataPart")

        try {
            // Act
            log.info("Building multipart request body")
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "scan",
                    "scan.json",
                    requestDataPart.toRequestBody("application/json".toMediaType())
                )
                .addFormDataPart(
                    "photos",
                    testFile.name,
                    testFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            log.info("Sending multipart POST request to /v1/composition/create/photos")
            val request = Request.Builder()
                .url("${getBaseUrl()}/v1/composition/create/photos")
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()

            // Assert
            log.info("Asserting response status is 200")
            Assertions.assertThat(response.code).isEqualTo(200)

            val responseBody: CompositionCreateByPhotosResponse = readResponse(response)

            log.info("Asserting response contains success result")
            Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
            Assertions.assertThat(responseBody.contextId).isNotEmpty()
            Assertions.assertThat(responseBody.contextId).isNotBlank()

            log.info("✅ Composition create photos test completed successfully")
        } finally {
            log.info("Cleaning up temporary test file")
            testFile.delete()
        }
    }
}