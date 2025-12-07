package net.otuskotlin.ingredientscan.api.v1.external.test

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalMapper
import net.otuskotlin.ingredientscan.api.v1.external.models.*
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs


private val TEST_COMPONENT = Component(
    id = "comp-001",
    name = "Пальмовое масло",
    riskLevel = RiskLevel.CRITICAL,
    healthRisks = "Сердечно-сосудистые заболевания, канцероген"
)

private val TEST_ANALYSIS = Analysis(
    id = "analysis-123",
    compositionId = "composition-123",
    createDate = OffsetDateTime.parse("2025-11-28T10:00:00Z"),
    description = "Высокий риск. Продукт содержит критические компоненты.",
    rating = 1.2,
    color = Analysis.Color.DARK_RED,
    problematicComponent = listOf(TEST_COMPONENT),
    safeComponent = emptyList(),
)

private val TEST_COMPOSITION = Composition(
    id = "composition-123",
    createDate = OffsetDateTime.parse("2025-11-28T09:00:00Z"),
    text = "Вода, сахар, пальмовое масло, краситель E100.",
    analysisId = "analysis-123",
    useCount = 5
)

private val DEBUG_MODE = RequestDebug(
    mode = DebugMode.STUB
)

class ApiV1SerializationTest {

    // analysisGet Request/Response

    @Test
    fun testAnalysisGetRequestSerialization() {
        val request = AnalysisGetRequest(requestType = "analysisGet", debug = DEBUG_MODE)
        val json = apiV1ExternalMapper.writeValueAsString(request)

        assertContains(json, "\"requestType\":\"analysisGet\"")
        assertContains(json, "\"mode\":\"stub\"")

        val obj = apiV1ExternalMapper.readValue(json, IRequest::class.java)
        assertIs<AnalysisGetRequest>(obj)
        assertEquals(DebugMode.STUB, obj.debug?.mode)
    }

    @Test
    fun testAnalysisGetResponseSerialization() {
        val response = AnalysisGetResponse(
            responseType = "analysisGet",
            result = ResponseResult.SUCCESS,
            analysis = TEST_ANALYSIS
        )
        val json = apiV1ExternalMapper.writeValueAsString(response)

        // Проверка ключевых полей
        assertContains(json, "\"responseType\":\"analysisGet\"")
        assertContains(json, "\"result\":\"success\"")
        assertContains(json, "\"rating\":1.2")

        // Проверка десериализации через базовый интерфейс
        val obj = apiV1ExternalMapper.readValue(json, IResponse::class.java)
        assertIs<AnalysisGetResponse>(obj)
        assertEquals(TEST_ANALYSIS.id, obj.analysis?.id)
    }

    //   compositionGet Request/Response

    @Test
    fun testCompositionGetRequestSerialization() {
        val request = CompositionGetRequest(requestType = "compositionGet", debug = DEBUG_MODE)
        val json = apiV1ExternalMapper.writeValueAsString(request)

        assertContains(json, "\"requestType\":\"compositionGet\"")

        val obj = apiV1ExternalMapper.readValue(json, IRequest::class.java)
        assertIs<CompositionGetRequest>(obj)
    }

    @Test
    fun testCompositionGetResponseSerialization() {
        val response = CompositionGetResponse(
            responseType = "compositionGet",
            result = ResponseResult.SUCCESS,
            composition = TEST_COMPOSITION
        )
        val json = apiV1ExternalMapper.writeValueAsString(response)

        assertContains(json, "\"responseType\":\"compositionGet\"")
        assertContains(json, "\"text\":\"Вода, сахар, пальмовое масло, краситель E100.\"") // ✅ Исправлено

        val obj = apiV1ExternalMapper.readValue(json, IResponse::class.java)
        assertIs<CompositionGetResponse>(obj)
        assertEquals(TEST_COMPOSITION.text, obj.composition?.text)
    }

    // compositionCreateByManual

    private val createManualRequest = CompositionCreateByManualRequest(
        requestType = "compositionCreateByManual",
        debug = DEBUG_MODE,
        scan = ScanManualDto(
            type = ScanType.MANUAL,
            text = "томаты измельченные 72%, концентрат томатный 14%, лук, масло подсолнечное рафинированное, базилик 2%, сахар, соль, регулятор кислотности."
        )
    )

    @Test
    fun testCompositionCreateByManualRequestDeserialization() {
        val json = apiV1ExternalMapper.writeValueAsString(createManualRequest)
        val obj = apiV1ExternalMapper.readValue(json, IRequest::class.java)

        assertIs<CompositionCreateByManualRequest>(obj)
        assertEquals(createManualRequest.scan?.text, obj.scan?.text)
    }

    // ErrorResponse

    @Test
    fun testErrorResponseSerialization() {
        val errorResponse = ErrorResponse(
            responseType = "error",
            result = ResponseResult.ERROR,
            errors = listOf(
                Error(
                    code = "validation-error",
                    group = "validation",
                    field = "scan.data",
                    message = "Текст состава слишком короткий"
                )
            )
        )
        val json = apiV1ExternalMapper.writeValueAsString(errorResponse)

        assertContains(json, "\"responseType\":\"error\"")
        assertContains(json, "\"result\":\"error\"")
        assertContains(json, "\"field\":\"scan.data\"")

        val obj = apiV1ExternalMapper.readValue(json, IResponse::class.java)
        assertIs<ErrorResponse>(obj)
        assertEquals("validation-error", obj.errors?.first()?.code)
    }

    //  IRequest

    @Test
    fun testRequestPolymorphism() {
        val jsonGet = apiV1ExternalMapper.writeValueAsString(AnalysisGetRequest(requestType = "analysisGet"))
        val jsonCreate = apiV1ExternalMapper.writeValueAsString(createManualRequest)

        val objGet = apiV1ExternalMapper.readValue(jsonGet, IRequest::class.java)
        val objCreate = apiV1ExternalMapper.readValue(jsonCreate, IRequest::class.java)

        assertIs<AnalysisGetRequest>(objGet)
        assertIs<CompositionCreateByManualRequest>(objCreate)
    }
}