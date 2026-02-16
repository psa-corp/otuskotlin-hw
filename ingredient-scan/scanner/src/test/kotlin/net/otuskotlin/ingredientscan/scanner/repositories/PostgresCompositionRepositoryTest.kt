package net.otuskotlin.ingredientscan.scanner.repositories

import io.mockk.*
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiFunction

class PostgresCompositionRepositoryTest {

    private lateinit var db: DatabaseClient
    private lateinit var repository: PostgresCompositionRepository

    @BeforeEach
    fun setUp() {
        db = mockk()
        repository = PostgresCompositionRepository(db)
    }

    @Test
    fun `save should execute insert with correct parameters`() = runTest {
        val composition = createComposition()
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", composition.id.asString()) } returns sqlSpec
        every { sqlSpec.bind("text", composition.text) } returns sqlSpec
        every { sqlSpec.bind("createDate", composition.createDate) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(1)

        repository.save(composition)

        assert(sqlSlot.captured.contains("INSERT INTO compositions"))
        verify(exactly = 1) { sqlSpec.bind("id", composition.id.asString()) }
        verify(exactly = 1) { sqlSpec.bind("text", composition.text) }
        verify(exactly = 1) { sqlSpec.bind("createDate", composition.createDate) }
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    @Test
    fun `findById should return composition when found`() = runTest {
        val id = IsCompositionId("test-id")
        val expected = createComposition(id = id)
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsComposition>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsComposition>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.just(expected)

        val result = repository.findById(id)

        assert(sqlSlot.captured.contains("SELECT .* FROM compositions WHERE id = :id".toRegex()))
        assertEquals(expected, result)
    }

    @Test
    fun `findById should return null when not found`() = runTest {
        val id = IsCompositionId("missing")
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsComposition>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsComposition>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.empty()

        val result = repository.findById(id)

        assert(sqlSlot.captured.contains("SELECT .* FROM compositions WHERE id = :id".toRegex()))
        assertNull(result)
    }


    @Test
    fun `findByText should return composition when found`() = runTest {
        val text = "unique text"
        val expected = createComposition(text = text)
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsComposition>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("text", text) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsComposition>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.just(expected)

        val result = repository.findByText(text)

        assert(sqlSlot.captured.contains("SELECT .* FROM compositions WHERE text = :text".toRegex()))
        assertEquals(expected, result)
    }

    @Test
    fun `findByText should return null when not found`() = runTest {
        val text = "missing"
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<IsComposition>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("text", text) } returns sqlSpec
        every { sqlSpec.map(any<BiFunction<Row, RowMetadata, IsComposition>>()) } returns fetchSpec
        every { fetchSpec.first() } returns Mono.empty()

        val result = repository.findByText(text)

        assert(sqlSlot.captured.contains("SELECT .* FROM compositions WHERE text = :text".toRegex()))
        assertNull(result)
    }

    @Test
    fun `delete should execute delete with correct id`() = runTest {
        val id = IsCompositionId("to-delete")
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.bind("id", id.asString()) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(1)

        repository.delete(id)

        assert(sqlSlot.captured.contains("DELETE FROM compositions WHERE id = :id"))
        verify(exactly = 1) { sqlSpec.bind("id", id.asString()) }
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    @Test
    fun `clear should delete all`() = runTest {
        val sqlSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        val fetchSpec = mockk<FetchSpec<Map<String, Any>>>()

        val sqlSlot = slot<String>()
        every { db.sql(capture(sqlSlot)) } returns sqlSpec
        every { sqlSpec.fetch() } returns fetchSpec
        coEvery { fetchSpec.rowsUpdated() } returns Mono.just(2)

        repository.clear()

        assert(sqlSlot.captured.contains("DELETE FROM compositions"))
        coVerify(exactly = 1) { fetchSpec.rowsUpdated() }
    }

    private fun createComposition(
        id: IsCompositionId = IsCompositionId(UUID.randomUUID().toString()),
        text: String = "test"
    ): IsComposition {
        return IsComposition(
            id = id,
            text = text,
            createDate = LocalDateTime.now()
        )
    }
}