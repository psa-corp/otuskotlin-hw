import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.IsStubs
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportAnalysisGet
import org.junit.Test
import kotlin.test.assertEquals

fun IsAnalysis.toTransportAnalysis(): Analysis? = this.toTransport()

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
        assertEquals(STUB_ANALYSIS.id, context.analysisIdRequest)
        assertEquals(IsCommand.ANALYSIS_GET, context.command)
    }

    @Test
    fun toTransportAnalysisGet() {
        val context = IsContext(
            requestId = IsRequestId("req-1234"),
            command = IsCommand.ANALYSIS_GET,
            analysisResponse = STUB_ANALYSIS,
            errors = mutableListOf(
                IsError(
                    code = "data-err",
                    group = "validation",
                    field = "analysisId",
                    message = "Invalid analysis ID",
                )
            ),
            state = IsState.FINISHING,
        )

        val response = context.toTransportAnalysisGet()

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