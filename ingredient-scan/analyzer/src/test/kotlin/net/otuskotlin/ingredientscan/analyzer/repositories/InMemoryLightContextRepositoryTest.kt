package net.otuskotlin.ingredientscan.analyzer.repositories

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryLightContextRepositoryTest {

    private lateinit var repository: InMemoryLightContextRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryLightContextRepository()
    }

    @Test
    fun `save and findById`() = runTest {
        val lightContext = createLightContext()
        repository.save(lightContext)

        val found = repository.findById(lightContext.id)
        assertEquals(lightContext, found)
    }

    @Test
    fun `findById returns null for non-existent id`() = runTest {
        val found = repository.findById(IsContextId("missing"))
        assertNull(found)
    }

    @Test
    fun `delete removes light context`() = runTest {
        val lightContext = createLightContext()
        repository.save(lightContext)

        repository.delete(lightContext.id)

        assertNull(repository.findById(lightContext.id))
    }

    @Test
    fun `delete does nothing for non-existent id`() = runTest {
        val lightContext = createLightContext()
        repository.save(lightContext)

        repository.delete(IsContextId("missing"))

        assertNotNull(repository.findById(lightContext.id))
    }

    @Test
    fun `clear removes all light contexts`() = runTest {
        val lightContext1 = createLightContext()
        val lightContext2 = createLightContext()
        repository.save(lightContext1)
        repository.save(lightContext2)

        repository.clear()

        assertNull(repository.findById(lightContext1.id))
        assertNull(repository.findById(lightContext2.id))
    }

    private fun createLightContext(): IsLightContext = IsLightContext(
        id = IsContextId(UUID.randomUUID().toString())
    )
}