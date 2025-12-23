package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByManualResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionCreateByPhotosResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.CompositionGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanManualDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanPhotosDto
import net.otuskotlin.ingredientscan.api.v1.external.models.ScanType
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseRestTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File

@DisplayName("REST API Tests - CompositionApi")
class CompositionApiRestTest : BaseRestTest() {

    private val log by lazy { LoggerFactory.getLogger(CompositionApiRestTest::class.java) }

    @Test
    fun `T-001 - composition create manual - success`() {
        // Given
        val request = CompositionCreateByManualRequest(
            requestType = "compositionCreateByManual",
            scan = ScanManualDto(
                text = "молоко, сахар, консервант E202",
                type = ScanType.MANUAL
            )
        )

        val requestBody = readRequest(request)
        log.info("Request body: $requestBody")

        // When
        val response = executePost(
            path = "/v1/composition/create/manual",
            body = requestBody
        )

        // Then
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionCreateByManualResponse = readResponse(response)
        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.contextId).isNotEmpty()
        Assertions.assertThat(responseBody.contextId).isNotBlank()
    }

    @Test
    fun `T-007 - composition get by id - success`() {
        // Given
        val compositionId = "comp-test-456"

        val request = CompositionGetRequest(
            requestType = "compositionGet",
            compositionId = compositionId
        )

        val requestBody = readRequest(request)
        log.info("Request body: $requestBody")

        val response = executePost(
            path = "/v1/composition/get",
            body = requestBody
        )

        // Then
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionGetResponse = readResponse(response)
        Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        Assertions.assertThat(responseBody.composition).isNotNull
        Assertions.assertThat(responseBody.composition?.id).isEqualTo(compositionId)
        Assertions.assertThat(responseBody.composition?.text).contains("молоко")
    }

    @Test
    fun `composition create photos - multipart upload - success`() {
        // Создаём временный файл для теста
        val testFile = File.createTempFile("test_label", ".jpg")
        testFile.writeBytes(ByteArray(1024)) // 1KB dummy file

        val request = CompositionCreateByPhotosRequest(
            requestType = "compositionCreateByPhotos",
            scan = ScanPhotosDto(
                type = ScanType.PHOTO,
            )
        )

        val requestDataPart = readRequest(request)
        log.info("Request body: $requestDataPart")


        try {
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "scan",
                    "scan.json",
                    requestDataPart.toRequestBody("application/json".toMediaType())  // указываем Content-Type
                )
                .addFormDataPart(
                    "photos",
                    testFile.name,
                    testFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("${getBaseUrl()}/v1/composition/create/photos")
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()

            Assertions.assertThat(response.code).isEqualTo(200)

            val responseBody: CompositionCreateByPhotosResponse = readResponse(response)
            Assertions.assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
            Assertions.assertThat(responseBody.contextId).isNotEmpty()
            Assertions.assertThat(responseBody.contextId).isNotBlank()
        } finally {
            // Cleanup
            testFile.delete()
        }
    }

}