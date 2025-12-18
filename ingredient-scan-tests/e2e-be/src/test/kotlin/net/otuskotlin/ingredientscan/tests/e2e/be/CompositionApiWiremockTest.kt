package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseWiremockTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

class CompositionApiWiremockTest : BaseWiremockTest() {
    private val log = LoggerFactory.getLogger(CompositionApiWiremockTest::class.java)

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
        assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionCreateByManualResponse = readResponse(response)
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.contextId).isNotEmpty()
        assertThat(responseBody.contextId).isNotBlank()
    }

    @Test
    fun `T-007 - composition get by id - success`() {
        // Given
        val compositionId = "composition_123"

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
        assertThat(response.code).isEqualTo(200)

        val responseBody: CompositionGetResponse = readResponse(response)
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.composition).isNotNull
        assertThat(responseBody.composition?.id).isEqualTo(compositionId)
        assertThat(responseBody.composition?.text).contains("молоко")
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
                    requestDataPart
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
                .build()

            val response = client.newCall(request).execute()

            assertThat(response.code).isEqualTo(200)

            val responseBody: CompositionCreateByPhotosResponse = readResponse(response)
            assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
            assertThat(responseBody.contextId).isNotEmpty()
            assertThat(responseBody.contextId).isNotBlank()
        } finally {
            // Cleanup
            testFile.delete()
        }
    }

}