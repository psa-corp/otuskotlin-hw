package net.otuskotlin.ingredientscan.app.repo.memory

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryCompositionRepositoryTest {

    private lateinit var repository: InMemoryCompositionRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryCompositionRepository()
    }

    @Test
    fun `save and findById`() = runTest {
        val composition = createComposition()
        repository.save(composition)

        val found = repository.findById(composition.id)
        assertEquals(composition, found)
    }

    @Test
    fun `findById returns null for non-existent id`() = runTest {
        val found = repository.findById(IsCompositionId("missing"))
        assertNull(found)
    }

    @Test
    fun `save and findByText`() = runTest {
        val text = "unique text"
        val composition = createComposition(text = text)
        repository.save(composition)

        val found = repository.findByText(text)
        assertEquals(composition, found)
    }

    @Test
    fun `findByText returns null for non-existent text`() = runTest {
        val found = repository.findByText("missing")
        assertNull(found)
    }

    @Test
    fun `save with same text overwrites previous`() = runTest {
        val text = "same text"
        val comp1 = createComposition(text = text)
        val comp2 = createComposition(text = text)
        repository.save(comp1)
        repository.save(comp2)

        val found = repository.findByText(text)
        assertEquals(comp2, found)
    }

    @Test
    fun `delete removes from both maps`() = runTest {
        val text = "delete me"
        val composition = createComposition(text = text)
        repository.save(composition)

        repository.delete(composition.id)

        assertNull(repository.findById(composition.id))
        assertNull(repository.findByText(text))
    }

    @Test
    fun `delete does nothing for non-existent id`() = runTest {
        val composition = createComposition()
        repository.save(composition)

        repository.delete(IsCompositionId("missing"))

        assertNotNull(repository.findById(composition.id))
    }

    @Test
    fun `clear removes all compositions`() = runTest {
        val comp1 = createComposition(text = "text1")
        val comp2 = createComposition(text = "text2")
        repository.save(comp1)
        repository.save(comp2)

        repository.clear()

        assertNull(repository.findById(comp1.id))
        assertNull(repository.findById(comp2.id))
        assertNull(repository.findByText("text1"))
        assertNull(repository.findByText("text2"))
    }

    private fun createComposition(text: String = "test"): IsComposition = IsComposition(
        id = IsCompositionId(UUID.randomUUID().toString()),
        text = text
    )
}