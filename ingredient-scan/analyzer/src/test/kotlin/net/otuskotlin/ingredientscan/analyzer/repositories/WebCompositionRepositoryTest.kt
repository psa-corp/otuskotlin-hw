package net.otuskotlin.ingredientscan.analyzer.repositories

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.analyzer.services.integration.internal.InternalApiClient
import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternalTransport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.UUID

class WebCompositionRepositoryTest {

    private lateinit var internalApiClient: InternalApiClient
    private lateinit var repository: WebCompositionRepository

    @BeforeEach
    fun setUp() {
        internalApiClient = mock()
        repository = WebCompositionRepository(internalApiClient)
    }

    @Test
    fun `save should call internalCompositionSave and return saved composition`() = runTest {
        val composition = createComposition()
        val internalComposition = composition.toInternalTransport()!!
        val response = InternalCompositionSaveResponse(
            responseType = "internalCompositionSave",
            result = InternalResponseResult.SUCCESS,
            composition = internalComposition
        )

        whenever(internalApiClient.internalCompositionSave(any<InternalCompositionSaveRequest>())).thenReturn(response)

        repository.save(composition)

        val captor = argumentCaptor<InternalCompositionSaveRequest>()
        verify(internalApiClient).internalCompositionSave(captor.capture())
        val capturedRequest = captor.firstValue
        assertEquals(internalComposition, capturedRequest.composition)
        assertEquals("internalCompositionSave", capturedRequest.requestType)
    }

    @Test
    fun `save should throw NullPointerException when internalCompositionSave returns null composition`() = runTest {
        val composition = createComposition()
        val response = InternalCompositionSaveResponse(
            responseType = "internalCompositionSave",
            result = InternalResponseResult.SUCCESS,
            composition = null
        )

        whenever(internalApiClient.internalCompositionSave(any<InternalCompositionSaveRequest>())).thenReturn(response)

        assertThrows<NullPointerException> {
            repository.save(composition)
        }
    }

    @Test
    fun `save should throw when internalApiClient throws exception`() = runTest {
        val composition = createComposition()
        val exception = RuntimeException("Network error")
        whenever(internalApiClient.internalCompositionSave(any<InternalCompositionSaveRequest>())).thenThrow(exception)

        val thrown = assertThrows<RuntimeException> {
            repository.save(composition)
        }
        assertEquals("Network error", thrown.message)
    }

    @Test
    fun `findByText should return composition when found`() = runTest {
        val text = "test composition"
        val composition = createComposition(text = text)
        val internalComposition = composition.toInternalTransport()!!
        val response = InternalCompositionFindResponse(
            responseType = "internalCompositionFind",
            result = InternalResponseResult.SUCCESS,
            composition = internalComposition
        )

        whenever(internalApiClient.internalCompositionFind(any<InternalCompositionFindRequest>())).thenReturn(response)

        val result = repository.findByText(text)

        assertEquals(composition, result)

        val captor = argumentCaptor<InternalCompositionFindRequest>()
        verify(internalApiClient).internalCompositionFind(captor.capture())
        val capturedRequest = captor.firstValue
        assertEquals(text, capturedRequest.text)
        assertEquals("internalCompositionFind", capturedRequest.requestType)
    }

    @Test
    fun `findByText should return null when composition not found`() = runTest {
        val text = "missing"
        val response = InternalCompositionFindResponse(
            responseType = "internalCompositionFind",
            result = InternalResponseResult.SUCCESS,
            composition = null
        )

        whenever(internalApiClient.internalCompositionFind(any<InternalCompositionFindRequest>())).thenReturn(response)

        val result = repository.findByText(text)

        assertNull(result)
    }

    @Test
    fun `findById should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.findById(IsCompositionId("any")) }
        }
    }

    @Test
    fun `delete should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.delete(IsCompositionId("any")) }
        }
    }

    @Test
    fun `clear should throw NotImplementedError`() {
        assertThrows<NotImplementedError> {
            runTest { repository.clear() }
        }
    }

    private fun createComposition(text: String = "test"): IsComposition = IsComposition(
        id = IsCompositionId(UUID.randomUUID().toString()),
        text = text
    )
}