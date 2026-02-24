package net.otuskotlin.ingredientscan.scanner.repositories

import io.mockk.*
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiFunction

class PostgresAnalysisRepositoryTest {

    private lateinit var db: DatabaseClient
    private lateinit var repository: PostgresAnalysisRepository
    private val FIXED_DATE = LocalDateTime.of(2026, 2, 15, 12, 0)

    @BeforeEach
    fun setUp() {
        db = mockk()
        repository = PostgresAnalysisRepository(db)
    }

    @Test
    fun `saveAnalysis should execute insert with correct parameters`() = runTest {
        val analysis = createAnalysis()
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        // Захватываем SQL для проверки
        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec

        every { sqlSpec.bind("id", analysis.id.asString()) } returns sqlSpec
        every { sqlSpec.bind("compositionId", analysis.compositionId.asString()) } returns sqlSpec
        every { sqlSpec.bind("createDate", analysis.createDate) } returns sqlSpec
        every { sqlSpec.bind("description", analysis.description) } returns sqlSpec
        every { sqlSpec.bind("rating", analysis.rating) } returns sqlSpec
        every { sqlSpec.bind("color", analysis.color.name) } returns sqlSpec
        every { sqlSpec.bind("components", any<String>()) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(1)

        repository.saveAnalysis(analysis)

        assert(sqlSlot.captured.contains("INSERT INTO analysis"))
        verify(exactly = 1) { sqlSpec.bind("id", analysis.id.asString()) }
        verify(exactly = 1) { sqlSpec.bind("compositionId", analysis.compositionId.asString()) }
        verify(exactly = 1) { sqlSpec.bind("createDate", analysis.createDate) }
        verify(exactly = 1) { sqlSpec.bind("description", analysis.description) }
        verify(exactly = 1) { sqlSpec.bind("rating", analysis.rating) }
        verify(exactly = 1) { sqlSpec.bind("color", analysis.color.name) }
        verify(exactly = 1) { sqlSpec.bind("components", any()) }
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    @Test
    fun `findAnalysisById should return analysis when found`() = runTest {
        val id = IsAnalysisId("test-id")
        val expected = createAnalysis(id = id).copy(createDate = FIXED_DATE)
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsAnalysis>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsAnalysis>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.just(expected)

        val result = repository.findAnalysisById(id)

        // Упрощённая проверка SQL: содержит ключевые слова
        assertTrue(sqlSlot.captured.contains("SELECT"))
        assertTrue(sqlSlot.captured.contains("FROM analysis"))
        assertTrue(sqlSlot.captured.contains("WHERE id = :id"))
        assertEquals(expected, result)
    }

    @Test
    fun `findAnalysisById should return null when not found`() = runTest {
        val id = IsAnalysisId("missing")
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsAnalysis>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsAnalysis>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.empty()

        val result = repository.findAnalysisById(id)

        assertTrue(sqlSlot.captured.contains("SELECT"))
        assertTrue(sqlSlot.captured.contains("FROM analysis"))
        assertTrue(sqlSlot.captured.contains("WHERE id = :id"))
        assertNull(result)
    }

    @Test
    fun `findAnalysisByCompositionId should return analysis when found`() = runTest {
        val compositionId = IsCompositionId("comp-id")
        val expected = createAnalysis(compositionId = compositionId).copy(createDate = FIXED_DATE)
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsAnalysis>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("compositionId", compositionId.asString()) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsAnalysis>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.just(expected)

        val result = repository.findAnalysisByCompositionId(compositionId)

        assertTrue(sqlSlot.captured.contains("SELECT"))
        assertTrue(sqlSlot.captured.contains("FROM analysis"))
        assertTrue(sqlSlot.captured.contains("WHERE composition_id = :compositionId"))
        assertEquals(expected, result)
    }

    @Test
    fun `deleteAnalysis should execute delete with correct id`() = runTest {
        val id = IsAnalysisId("to-delete")
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(1)

        repository.deleteAnalysis(id)

        assert(sqlSlot.captured.contains("DELETE FROM analysis WHERE id = :id"))
        verify(exactly = 1) { sqlSpec.bind("id", id.asString()) }
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    @Test
    fun `clearAnalysis should delete all`() = runTest {
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(3)

        repository.clearAnalysis()

        assert(sqlSlot.captured.contains("DELETE FROM analysis"))
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    private fun createAnalysis(
        id: IsAnalysisId = IsAnalysisId(UUID.randomUUID().toString()),
        compositionId: IsCompositionId = IsCompositionId(UUID.randomUUID().toString())
    ): IsAnalysis {
        return IsAnalysis(
            id = id,
            compositionId = compositionId,
            createDate = FIXED_DATE,
            description = "test",
            rating = 4.5,
            color = IsColor.GREEN
        )
    }
}