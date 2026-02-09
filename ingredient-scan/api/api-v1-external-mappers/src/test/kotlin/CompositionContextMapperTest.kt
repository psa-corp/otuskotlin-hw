import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.IsStubs
import net.otuskotlin.ingredientscan.mappers.v1.external.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransport
import net.otuskotlin.ingredientscan.mappers.v1.external.toTransportCompositionContextGet
import org.junit.Test
import kotlin.test.assertEquals
import java.time.LocalDateTime
import kotlin.test.assertNotNull

fun IsCompositionContext.toTransportCompositionContext(): CompositionContext? = this.toTransport()

class CompositionContextMapperTest {

    @Test
    fun fromTransportCompositionContextGet() {
        val contextId = "context_12345"
        val req = CompositionContextGetRequest(
            requestType = "compositionContextGet",
            debug = RequestDebug(
                mode = DebugMode.STUB,
                stub = RequestDebugStub.SUCCESS,
            ),
            contextId = contextId,
        )
        val context = IsContext()
        context.fromTransport(req)

        assertEquals(IsStubs.SUCCESS, context.stubCase)
        assertEquals(IsWorkMode.STUB, context.workMode)
        assertEquals(IsContextId(contextId), context.contextIdRequest)
        assertEquals(IsCommand.COMPOSITION_CONTEXT_GET, context.command)
    }

    @Test
    fun toTransportCompositionContextGet() {
        val compositionContext = IsCompositionContext(
            id = IsContextId("context_5678"),
            state = IsState.FINISHING,
            errors = mutableListOf(
                IsError(
                    code = "comp-err",
                    group = "processing",
                    field = "composition",
                    message = "Composition processing completed",
                )
            ),
            timeStart = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            composition = STUB_COMPOSITION,
        )

        val context = IsContext(
            requestId = IsRequestId("req-5678"),
            command = IsCommand.COMPOSITION_CONTEXT_GET,
            compositionContextResponse = compositionContext,
            errors = mutableListOf(),
            state = IsState.FINISHING,
        )

        val response = context.toTransportCompositionContextGet()

        // Проверка метаданных ответа
        assertEquals(ResponseResult.SUCCESS, response.result)
        assertEquals("compositionContextGet", response.responseType)

        // Проверка ошибок
        assertEquals(null, response.errors)

        // Проверка маппинга объекта CompositionContext
        val transportContext = response.context
        assertNotNull(transportContext)
        assertEquals("context_5678", transportContext?.id)

        // Проверка вложенного состава
        val transportComposition = transportContext?.composition
        assertNotNull(transportComposition)
        assertEquals(STUB_COMPOSITION.id.asString(), transportComposition?.id)
        assertEquals(STUB_COMPOSITION.text, transportComposition?.text)
    }

    @Test
    fun toTransportCompositionContextGetWithErrors() {
        val compositionContext = IsCompositionContext(
            id = IsContextId("context_error"),
            state = IsState.FAILING,
            errors = mutableListOf(
                IsError(
                    code = "not_found",
                    group = "db",
                    field = "contextId",
                    message = "Context not found in database",
                )
            ),
            timeStart = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            composition = IsComposition(),
        )

        val context = IsContext(
            requestId = IsRequestId("req-error"),
            command = IsCommand.COMPOSITION_CONTEXT_GET,
            compositionContextResponse = compositionContext,
            errors = mutableListOf(
                IsError(
                    code = "validation_error",
                    group = "input",
                    field = "contextId",
                    message = "Invalid context ID format",
                )
            ),
            state = IsState.FAILING,
        )

        val response = context.toTransportCompositionContextGet()

        // Проверка метаданных ответа
        assertEquals(ResponseResult.ERROR, response.result)
        assertEquals("compositionContextGet", response.responseType)

        // Проверка ошибок
        assertEquals(1, response.errors?.size)
        val error = response.errors?.firstOrNull()
        assertEquals("validation_error", error?.code)
        assertEquals("input", error?.group)
        assertEquals("contextId", error?.field)
        assertEquals("Invalid context ID format", error?.message)

        // Проверка, что контекст в ответе
        assertNotNull(response.context)
        assertEquals("context_error", response.context?.id)
    }
}
