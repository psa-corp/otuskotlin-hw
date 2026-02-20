package net.otuskotlin.ingredientscan.app.repo.memory

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

// Имитируем Elasticsearch или PostgreSQL

@Repository("memoryAnalysisRepo")
open class InMemoryAnalysisRepository : IsAnalysisRepository {
    private val log = LoggerFactory.getLogger(InMemoryAnalysisRepository::class.java)

    private val storeByAnalysis = ConcurrentHashMap<IsAnalysisId, IsAnalysis>()
    private val storeByComposition = ConcurrentHashMap<IsCompositionId, IsAnalysis>()

    override suspend fun saveAnalysis(analysis: IsAnalysis) {
        storeByAnalysis[analysis.id] = analysis
        storeByComposition[analysis.compositionId] = analysis
        log.info("Saved analysis: $analysis.id")
    }

    override suspend fun findAnalysisById(id: IsAnalysisId): IsAnalysis? {
        return storeByAnalysis[id]
    }

    override suspend fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis? {
        return storeByComposition[id]
    }


    override suspend fun updateAnalysis(analysis: IsAnalysis) {
        saveAnalysis(analysis)
    }

    override suspend fun deleteAnalysis(id: IsAnalysisId) {
        val analysis = storeByAnalysis[id]
        if (analysis != null) {
            storeByAnalysis.remove(id)
            storeByComposition.remove(analysis.compositionId)
            log.info("Delete analysis: ${analysis.id}")
        }
    }

    override suspend fun clearAnalysis() {
        storeByAnalysis.clear()
        storeByComposition.clear()
    }
}