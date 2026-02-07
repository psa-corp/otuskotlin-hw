package net.otuskotlin.ingredientscan.mappers.v1.internal

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysis
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisFindRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisSaveRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalColor
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponseResult
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.fromTransport
import net.otuskotlin.ingredientscan.mappers.v1.toTransportInternalAnalysisFind
import net.otuskotlin.ingredientscan.mappers.v1.toTransportInternalAnalysisSave
import net.otuskotlin.ingredientscan.mappers.v1.toInternalTransport
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun IsAnalysis.toInternalTransportAnalysis(): InternalAnalysis? = this.toInternalTransport()

class InternalAnalysisMapperTest {

    private val TEST_INTERNAL_ANALYSIS = IsAnalysis(
        id = IsAnalysisId("analysis-123"),
        compositionId = IsCompositionId("comp-123"),
        createDate = LocalDateTime.of(2025, 11, 28, 10, 0),
        description = "Test Description",
        rating = 4.5,
        color = IsColor.GREEN
    )

    private val TEST_TRANSPORT_ANALYSIS = InternalAnalysis(
        id = "analysis-123",
        compositionId = "comp-123",
        createDate = LocalDateTime.of(2025, 11, 28, 10, 0).atOffset(ZoneOffset.UTC),
        description = "Test Description",
        rating = 4.5,
        color = InternalColor.GREEN,
        problematicComponent = mutableListOf(),
        safeComponent = mutableListOf()
    )

    @Test
    fun fromTransportInternalAnalysisFind() {
        val req = InternalAnalysisFindRequest(
            requestType = "internalAnalysisFind",
            compositionId = "comp-123"
        )

        val context = InternalContext()
        context.fromTransport(req)

        assertEquals(InternalCommand.ANALYSIS_FIND, context.command)
        assertEquals(IsCompositionId("comp-123"), context.compositionIdRequest)
    }

    @Test
    fun fromTransportInternalAnalysisSave() {
        val req = InternalAnalysisSaveRequest(
            requestType = "internalAnalysisSave",
            analysis = TEST_TRANSPORT_ANALYSIS
        )

        val context = InternalContext()
        context.fromTransport(req)

        assertEquals(InternalCommand.ANALYSIS_SAVE, context.command)
        assertEquals(TEST_INTERNAL_ANALYSIS.id, context.analysisRequest.id)
        assertEquals(TEST_INTERNAL_ANALYSIS.compositionId, context.analysisRequest.compositionId)
        assertEquals(TEST_INTERNAL_ANALYSIS.rating, context.analysisRequest.rating)
        assertEquals(TEST_INTERNAL_ANALYSIS.color, context.analysisRequest.color)
    }

    @Test
    fun toTransportInternalAnalysisFind() {
        val context = InternalContext(
            command = InternalCommand.ANALYSIS_FIND,
            state = IsState.FINISHING,
            analysisResponse = TEST_INTERNAL_ANALYSIS,
            errors = mutableListOf()
        )

        val response = context.toTransportInternalAnalysisFind()

        assertEquals("internalAnalysisFind", response.responseType)
        assertEquals(InternalResponseResult.SUCCESS, response.result)
        assertEquals(null, response.errors)

        val analysis = response.analysis
        assertNotNull(analysis)
        assertEquals(TEST_INTERNAL_ANALYSIS.id.asString(), analysis.id)
        assertEquals(TEST_INTERNAL_ANALYSIS.rating, analysis.rating)
        assertEquals(InternalColor.GREEN, analysis.color)
    }

    @Test
    fun toTransportInternalAnalysisSave() {
        val context = InternalContext(
            command = InternalCommand.ANALYSIS_SAVE,
            state = IsState.FINISHING,
            analysisResponse = TEST_INTERNAL_ANALYSIS,
            errors = mutableListOf(
                IsError(code = "warning", message = "Low confidence")
            )
        )

        val response = context.toTransportInternalAnalysisSave()

        assertEquals("internalAnalysisSave", response.responseType)
        assertEquals(InternalResponseResult.SUCCESS, response.result)

        assertEquals(1, response.errors?.size)
        assertEquals("warning", response.errors?.first()?.code)

        val analysis = response.analysis
        assertNotNull(analysis)
        assertEquals(TEST_INTERNAL_ANALYSIS.id.asString(), analysis.id)
    }
}
