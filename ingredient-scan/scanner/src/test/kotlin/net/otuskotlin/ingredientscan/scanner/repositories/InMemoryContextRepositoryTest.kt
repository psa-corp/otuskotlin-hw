package net.otuskotlin.ingredientscan.scanner.repositories

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryContextRepositoryTest {

    private lateinit var repository: InMemoryContextRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryContextRepository()
    }

    @Test
    fun `save and findById`() = runTest {
        val context = createContext()
        repository.save(context)

        val found = repository.findById(context.id)
        assertEquals(context, found)
    }

    @Test
    fun `findById returns null for non-existent id`() = runTest {
        val found = repository.findById(IsContextId("missing"))
        assertNull(found)
    }

    @Test
    fun `delete removes context`() = runTest {
        val context = createContext()
        repository.save(context)

        repository.delete(context.id)

        assertNull(repository.findById(context.id))
    }

    @Test
    fun `delete does nothing for non-existent id`() = runTest {
        val context = createContext()
        repository.save(context)

        repository.delete(IsContextId("missing"))

        assertNotNull(repository.findById(context.id))
    }

    @Test
    fun `clear removes all contexts`() = runTest {
        val context1 = createContext()
        val context2 = createContext()
        repository.save(context1)
        repository.save(context2)

        repository.clear()

        assertNull(repository.findById(context1.id))
        assertNull(repository.findById(context2.id))
    }

    private fun createContext(): IsContext = IsContext(
        id = IsContextId(UUID.randomUUID().toString())
    )
}