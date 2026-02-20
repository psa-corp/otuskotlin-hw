package net.otuskotlin.ingredientscan.analyzer.repositories

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.analyzer.services.integration.internal.InternalApiClient
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternalTransport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.UUID

class WebAnalysisRepositoryTest {

    private lateinit var internalApiClient: InternalApiClient
    private lateinit var repository: WebAnalysisRepository

    @BeforeEach
    fun setUp() {
        internalApiClient = mock()
        repository = WebAnalysisRepository(internalApiClient)
    }

    @Test
    fun `saveAnalysis should call internalAnalysisSave and return saved analysis`() = runTest {
        val analysis = createAnalysis()
        val internalAnalysis = analysis.toInternalTransport()!!
        val response = InternalAnalysisSaveResponse(
            responseType = "internalAnalysisSave",
            result = InternalResponseResult.SUCCESS,
            analysis = internalAnalysis
        )

        whenever(internalApiClient.internalAnalysisSave(any<InternalAnalysisSaveRequest>())).thenReturn(response)

        repository.saveAnalysis(analysis)

        val captor = argumentCaptor<InternalAnalysisSaveRequest>()
        verify(internalApiClient).internalAnalysisSave(captor.capture())
        val capturedRequest = captor.firstValue
        assertEquals(internalAnalysis, capturedRequest.analysis)
        assertEquals("internalAnalysisSave", capturedRequest.requestType)
    }

    @Test
    fun `saveAnalysis should throw NullPointerException when internalAnalysisSave returns null analysis`() = runTest {
        val analysis = createAnalysis()
        val response = InternalAnalysisSaveResponse(
            responseType = "internalAnalysisSave",
            result = InternalResponseResult.SUCCESS,
            analysis = null
        )

        whenever(internalApiClient.internalAnalysisSave(any<InternalAnalysisSaveRequest>())).thenReturn(response)

        assertThrows<NullPointerException> {
            repository.saveAnalysis(analysis)
        }
    }

    @Test
    fun `saveAnalysis should throw when internalApiClient throws exception`() = runTest {
        val analysis = createAnalysis()
        val exception = RuntimeException("Network error")
        whenever(internalApiClient.internalAnalysisSave(any<InternalAnalysisSaveRequest>())).thenThrow(exception)

        val thrown = assertThrows<RuntimeException> {
            repository.saveAnalysis(analysis)
        }
        assertEquals("Network error", thrown.message)
    }

    @Test
    fun `findAnalysisByCompositionId should return analysis when found`() = runTest {
        val compositionId = IsCompositionId(UUID.randomUUID().toString())
        val analysis = createAnalysis(compositionId = compositionId)
        val internalAnalysis = analysis.toInternalTransport()!!
        val response = InternalAnalysisFindResponse(
            responseType = "internalAnalysisFind",
            result = InternalResponseResult.SUCCESS,
            analysis = internalAnalysis
        )

        whenever(internalApiClient.internalAnalysisFind(any<InternalAnalysisFindRequest>())).thenReturn(response)

        val result = repository.findAnalysisByCompositionId(compositionId)

        assertEquals(analysis, result)

        val captor = argumentCaptor<InternalAnalysisFindRequest>()
        verify(internalApiClient).internalAnalysisFind(captor.capture())
        val capturedRequest = captor.firstValue
        assertEquals(compositionId.asString(), capturedRequest.compositionId)
        assertEquals("internalAnalysisFind", capturedRequest.requestType)
    }

    @Test
    fun `findAnalysisByCompositionId should return null when analysis not found`() = runTest {
        val compositionId = IsCompositionId("missing")
        val response = InternalAnalysisFindResponse(
            responseType = "internalAnalysisFind",
            result = InternalResponseResult.SUCCESS,
            analysis = null
        )

        whenever(internalApiClient.internalAnalysisFind(any<InternalAnalysisFindRequest>())).thenReturn(response)

        val result = repository.findAnalysisByCompositionId(compositionId)

        assertNull(result)
    }

    @Test
    fun `findAnalysisById should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.findAnalysisById(IsAnalysisId("any")) }
        }
    }

    @Test
    fun `updateAnalysis should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.updateAnalysis(createAnalysis()) }
        }
    }

    @Test
    fun `deleteAnalysis should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.deleteAnalysis(IsAnalysisId("any")) }
        }
    }

    @Test
    fun `clearAnalysis should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.clearAnalysis() }
        }
    }

    private fun createAnalysis(compositionId: IsCompositionId = IsCompositionId(UUID.randomUUID().toString())): IsAnalysis =
        IsAnalysis(
            id = IsAnalysisId(UUID.randomUUID().toString()),
            compositionId = compositionId,
            description = "test analysis"
        )
}