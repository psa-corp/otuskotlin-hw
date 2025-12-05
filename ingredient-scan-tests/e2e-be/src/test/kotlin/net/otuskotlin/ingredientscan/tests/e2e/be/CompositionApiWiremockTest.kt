package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseWiremockTest
import net.otuskotlin.ingredientscan.tests.e2e.be.models.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class CompositionApiWiremockTest : BaseWiremockTest() {

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

        // When
        val response = executePost(
            path = "/v1/composition/create/manual",
            body = mapper.writeValueAsString(request)
        )

        // Then
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody = mapper.readValue<CompositionCreateByManualResponse>(response.body!!.string())
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.compositionId).isNotEmpty()
        assertThat(responseBody.compositionId).isNotBlank()
    }

    @Test
    fun `T-007 - composition get by id - success`() {
        // Given
        val compositionId = "composition_123"
        val request = CompositionGetRequest(
            requestType = "compositionGet"
        )

        // When
        val response = executePost(
            path = "/v1/composition/get/$compositionId",
            body = mapper.writeValueAsString(request)
        )

        // Then
        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody = mapper.readValue<CompositionGetResponse>(response.body!!.string())
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.composition).isNotNull
        assertThat(responseBody.composition?.id).isEqualTo(compositionId)
        assertThat(responseBody.composition?.text).contains("молоко")
        assertThat(responseBody.composition?.useCount).isGreaterThan(0)
    }

    @Test
    fun `composition create photos - multipart upload - success`() {
        // Тест загрузки фото (требует реального файла в тестовых ресурсах)
        val testFile = File("src/test/resources/test_label.jpg")
        if (!testFile.exists()) {
            // Создаем временный файл для теста
            testFile.parentFile.mkdirs()
            testFile.writeBytes(ByteArray(1024)) // 1KB dummy file
        }

        // Create multipart request
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.Companion.FORM)
            .addFormDataPart(
                "scan",
                mapper.writeValueAsString(
                    CompositionCreateByPhotosRequest(
                        requestType = "compositionCreateByPhotos",
                        scan = ScanPhotosDto(
                            type = ScanType.PHOTO
                        )
                    )
                )
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

        Assertions.assertThat(response.code).isEqualTo(200)

        val responseBody = mapper.readValue<CompositionCreateByPhotosResponse>(response.body!!.string())
        assertThat(responseBody.result).isEqualTo(ResponseResult.SUCCESS)
        assertThat(responseBody.compositionId).isNotEmpty()
    }
}