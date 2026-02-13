package net.otuskotlin.ingredientscan.scanner.repositories

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryAnalysisRepositoryTest {

    private lateinit var repository: InMemoryAnalysisRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryAnalysisRepository()
    }

    @Test
    fun `save and findById`() = runTest {
        val analysis = createAnalysis()
        repository.saveAnalysis(analysis)

        val found = repository.findAnalysisById(analysis.id)
        assertEquals(analysis, found)
    }

    @Test
    fun `findById returns null for non-existent id`() = runTest {
        val found = repository.findAnalysisById(IsAnalysisId("missing"))
        assertNull(found)
    }

    @Test
    fun `save and findByCompositionId`() = runTest {
        val analysis = createAnalysis()
        repository.saveAnalysis(analysis)

        val found = repository.findAnalysisByCompositionId(analysis.compositionId)
        assertEquals(analysis, found)
    }

    @Test
    fun `findByCompositionId returns null for non-existent composition`() = runTest {
        val found = repository.findAnalysisByCompositionId(IsCompositionId("missing"))
        assertNull(found)
    }

    @Test
    fun `updateAnalysis overwrites existing`() = runTest {
        val analysis = createAnalysis()
        repository.saveAnalysis(analysis)

        val updated = analysis.copy(description = "updated")
        repository.updateAnalysis(updated)

        val found = repository.findAnalysisById(analysis.id)
        assertEquals(updated, found)
    }

    @Test
    fun `updateAnalysis works when analysis does not exist`() = runTest {
        val analysis = createAnalysis()
        repository.updateAnalysis(analysis)

        val found = repository.findAnalysisById(analysis.id)
        assertEquals(analysis, found)
    }

    @Test
    fun `deleteAnalysis removes from both maps`() = runTest {
        val analysis = createAnalysis()
        repository.saveAnalysis(analysis)

        repository.deleteAnalysis(analysis.id)

        assertNull(repository.findAnalysisById(analysis.id))
        assertNull(repository.findAnalysisByCompositionId(analysis.compositionId))
    }

    @Test
    fun `deleteAnalysis does nothing for non-existent id`() = runTest {
        val analysis = createAnalysis()
        repository.saveAnalysis(analysis)

        repository.deleteAnalysis(IsAnalysisId("missing"))

        assertNotNull(repository.findAnalysisById(analysis.id))
    }

    @Test
    fun `clear removes all analyses`() = runTest {
        val analysis1 = createAnalysis()
        val analysis2 = createAnalysis()
        repository.saveAnalysis(analysis1)
        repository.saveAnalysis(analysis2)

        repository.clearAnalysis()

        assertNull(repository.findAnalysisById(analysis1.id))
        assertNull(repository.findAnalysisById(analysis2.id))
    }

    private fun createAnalysis(): IsAnalysis = IsAnalysis(
        id = IsAnalysisId(UUID.randomUUID().toString()),
        compositionId = IsCompositionId(UUID.randomUUID().toString()),
        description = "test analysis"
    )
}