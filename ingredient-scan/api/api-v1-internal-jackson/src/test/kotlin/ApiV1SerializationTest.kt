package net.otuskotlin.ingredientscan.api.v1.internal.test

import net.otuskotlin.ingredientscan.api.v1.internal.apiV1InternalMapper
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

private val TEST_INTERNAL_COMPONENT = InternalComponent(
    name = "Пальмовое масло",
    riskLevel = InternalRiskLevel.CRITICAL,
    healthRisks = "Сердечно-сосудистые заболевания, канцероген",
    scientificName = "Elaeis guineensis",
    description = "Растительное масло",
    sources = "Wiki"
)

private val TEST_INTERNAL_ANALYSIS = InternalAnalysis(
    id = "analysis-123",
    compositionId = "composition-123",
    createDate = OffsetDateTime.parse("2025-11-28T10:00:00Z"),
    description = "Высокий риск. Продукт содержит критические компоненты.",
    rating = 1.2,
    color = InternalColor.DARK_RED,
    components = listOf(TEST_INTERNAL_COMPONENT)
)

private val TEST_INTERNAL_COMPOSITION = InternalComposition(
    id = "composition-123",
    createDate = OffsetDateTime.parse("2025-11-28T09:00:00Z"),
    text = "Вода, сахар, пальмовое масло, краситель E100."
)

class ApiV1InternalSerializationTest {

    // internalAnalysisFind Request/Response
    @Test
    fun testInternalAnalysisFindRequestSerialization() {
        val request = InternalAnalysisFindRequest(
            requestType = "internalAnalysisFind",
            compositionId = "composition-123"
        )

        val json = apiV1InternalMapper.writeValueAsString(request)

        assertContains(json, "\"requestType\":\"internalAnalysisFind\"")
        assertContains(json, "\"compositionId\":\"composition-123\"")

        val obj = apiV1InternalMapper.readValue(json, InternalRequest::class.java)
        assertIs<InternalAnalysisFindRequest>(obj)
        assertEquals("composition-123", obj.compositionId)
    }

    @Test
    fun testInternalAnalysisFindResponseSerialization() {
        val response = InternalAnalysisFindResponse(
            responseType = "internalAnalysisFind",
            result = InternalResponseResult.SUCCESS,
            analysis = TEST_INTERNAL_ANALYSIS
        )

        val json = apiV1InternalMapper.writeValueAsString(response)

        // Проверка ключевых полей
        assertContains(json, "\"responseType\":\"internalAnalysisFind\"")
        assertContains(json, "\"result\":\"success\"")
        assertContains(json, "\"rating\":1.2")

        // Проверка десериализации через базовый интерфейс
        val obj = apiV1InternalMapper.readValue(json, InternalResponse::class.java)
        assertIs<InternalAnalysisFindResponse>(obj)
        assertEquals(TEST_INTERNAL_ANALYSIS.id, obj.analysis?.id)
    }

    // internalCompositionFind Request/Response
    @Test
    fun testInternalCompositionFindRequestSerialization() {
        val request = InternalCompositionFindRequest(
            requestType = "internalCompositionFind",
            text = "Соль, сахар"
        )

        val json = apiV1InternalMapper.writeValueAsString(request)

        assertContains(json, "\"requestType\":\"internalCompositionFind\"")
        assertContains(json, "\"text\":\"Соль, сахар\"")

        val obj = apiV1InternalMapper.readValue(json, InternalRequest::class.java)
        assertIs<InternalCompositionFindRequest>(obj)
        assertEquals("Соль, сахар", obj.text)
    }

    @Test
    fun testInternalCompositionFindResponseSerialization() {
        val response = InternalCompositionFindResponse(
            responseType = "internalCompositionFind",
            result = InternalResponseResult.SUCCESS,
            composition = TEST_INTERNAL_COMPOSITION
        )

        val json = apiV1InternalMapper.writeValueAsString(response)

        assertContains(json, "\"responseType\":\"internalCompositionFind\"")
        assertContains(json, "\"text\":\"Вода, сахар, пальмовое масло, краситель E100.\"")

        val obj = apiV1InternalMapper.readValue(json, InternalResponse::class.java)
        assertIs<InternalCompositionFindResponse>(obj)
        assertEquals(TEST_INTERNAL_COMPOSITION.text, obj.composition?.text)
    }

    // internalCompositionSave Request
    @Test
    fun testInternalCompositionSaveRequestSerialization() {
        val request = InternalCompositionSaveRequest(
            requestType = "internalCompositionSave",
            composition = TEST_INTERNAL_COMPOSITION
        )

        val json = apiV1InternalMapper.writeValueAsString(request)

        assertContains(json, "\"requestType\":\"internalCompositionSave\"")

        val obj = apiV1InternalMapper.readValue(json, InternalRequest::class.java)
        assertIs<InternalCompositionSaveRequest>(obj)
        assertEquals(TEST_INTERNAL_COMPOSITION.id, obj.composition?.id)
    }

    // InternalErrorResponse
    @Test
    fun testInternalErrorResponseSerialization() {
        val errorResponse = InternalErrorResponse(
            responseType = "error",
            result = InternalResponseResult.ERROR,
            errors = listOf(
                InternalError(
                    code = "db-error",
                    group = "database",
                    field = "id",
                    message = "Объект не найден"
                )
            )
        )

        val json = apiV1InternalMapper.writeValueAsString(errorResponse)

        assertContains(json, "\"responseType\":\"error\"")
        assertContains(json, "\"result\":\"error\"")
        assertContains(json, "\"code\":\"db-error\"")

        val obj = apiV1InternalMapper.readValue(json, InternalResponse::class.java)
        assertIs<InternalErrorResponse>(obj)
        assertEquals("db-error", obj.errors?.first()?.code)
    }

    // InternalRequest Polymorphism
    @Test
    fun testInternalRequestPolymorphism() {
        val jsonFind = apiV1InternalMapper.writeValueAsString(
            InternalAnalysisFindRequest(requestType = "internalAnalysisFind", compositionId = "123")
        )
        val jsonSave = apiV1InternalMapper.writeValueAsString(
            InternalCompositionSaveRequest(
                requestType = "internalCompositionSave",
                composition = TEST_INTERNAL_COMPOSITION
            )
        )

        val objFind = apiV1InternalMapper.readValue(jsonFind, InternalRequest::class.java)
        val objSave = apiV1InternalMapper.readValue(jsonSave, InternalRequest::class.java)

        assertIs<InternalAnalysisFindRequest>(objFind)
        assertIs<InternalCompositionSaveRequest>(objSave)
    }
}
