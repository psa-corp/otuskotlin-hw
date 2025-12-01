import net.otuskotlin.ingredientscan.api.v1.external.models.Analysis
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.IsContext
import net.otuskotlin.ingredientscan.core.common.models.*
import net.otuskotlin.ingredientscan.mappers.v1.toTransportAnalysisGet // Ваша исправленная функция
import org.junit.Test
import net.otuskotlin.ingredientscan.api.v1.external.models.AnalysisGetRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.RequestDebug
import net.otuskotlin.ingredientscan.core.common.stubs.IsStubs
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import kotlin.test.assertEquals

import net.otuskotlin.ingredientscan.api.v1.external.models.DebugMode
import net.otuskotlin.ingredientscan.api.v1.external.models.RequestDebugStub
import java.time.LocalDateTime

// Простая заглушка для тестирования (в реальном проекте вынесена в IsAnalysisStub) (Запланирована)
val STUB_ANALYSIS: IsAnalysis = IsAnalysis(
    id = IsAnalysisId("analysis-test-123"),
    compositionId = IsCompositionId("comp-test-456"),
    createDate = LocalDateTime.now(),
    description = "Test analysis description",
    rating = 4.5,
    color = IsColor.GREEN,
)

// Предполагаем, что есть функция-расширение для маппинга IsAnalysis -> Analysis?
fun IsAnalysis.toTransportAnalysis(): Analysis? = this.toTransport() // или соответствующая функция

class AnalysisMapperTest {

    @Test
    fun fromTransportAnalysisGet() {
        val req = AnalysisGetRequest(
            requestType = "analysisGet",
            debug = RequestDebug(
                mode = DebugMode.STUB,
                stub = RequestDebugStub.SUCCESS,
            ),
            analysisId = STUB_ANALYSIS.id.asString(),
        )
        val context = IsContext()
        context.fromTransport(req)

        assertEquals(IsStubs.SUCCESS, context.stubCase)
        assertEquals(IsWorkMode.STUB, context.workMode)
        assertEquals(STUB_ANALYSIS.id, context.analysisRequest.id)
        assertEquals(IsCommand.ANALYSIS_GET, context.command)
    }

    // -----------------------------

    @Test
    fun toTransportAnalysisGet() {
        // 1. Создание контекста с данными ответа и ошибками
        val context = IsContext(
            requestId = IsRequestId("req-1234"),
            command = IsCommand.ANALYSIS_GET,
            analysisResponse = STUB_ANALYSIS, // Наш ответный объект
            errors = mutableListOf(
                IsError(
                    code = "data-err",
                    group = "validation",
                    field = "analysisId",
                    message = "Invalid analysis ID",
                )
            ),
            state = IsState.FINISHING, // Маппится в ResponseResult.SUCCESS
        )

        // 2. Вызов функции маппинга
        val response = context.toTransportAnalysisGet() as AnalysisGetResponse

        // 3. Проверки

        // Проверка метаданных ответа
        assertEquals(ResponseResult.SUCCESS, response.result)
        assertEquals("analysisGet", response.responseType)

        // Проверка ошибок
        assertEquals(1, response.errors?.size)
        val error = response.errors?.firstOrNull()
        assertEquals("data-err", error?.code)
        assertEquals("validation", error?.group)
        assertEquals("analysisId", error?.field)
        assertEquals("Invalid analysis ID", error?.message)

        // Проверка маппинга объекта Analysis
        val analysis = response.analysis
        assertEquals(STUB_ANALYSIS.toTransportAnalysis(), analysis)
        assertEquals(STUB_ANALYSIS.id.asString(), analysis?.id)
        assertEquals(STUB_ANALYSIS.description, analysis?.description)
        assertEquals(STUB_ANALYSIS.rating, analysis?.rating)
    }
}