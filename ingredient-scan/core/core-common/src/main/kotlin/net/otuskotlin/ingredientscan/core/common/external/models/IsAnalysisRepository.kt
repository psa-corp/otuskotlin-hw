package net.otuskotlin.ingredientscan.core.common.external.models

interface IsAnalysisRepository {
    suspend fun saveAnalysis(analysis: IsAnalysis)
    suspend fun findAnalysisById(id: IsAnalysisId): IsAnalysis?
    suspend fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis?
    suspend fun updateAnalysis(analysis: IsAnalysis)
    suspend fun deleteAnalysis(id: IsAnalysisId)
    suspend fun clearAnalysis()

    companion object {
        val NONE = object : IsAnalysisRepository {
            override suspend fun saveAnalysis(analysis: IsAnalysis) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findAnalysisById(id: IsAnalysisId): IsAnalysis? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun updateAnalysis(analysis: IsAnalysis) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun deleteAnalysis(id: IsAnalysisId) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clearAnalysis() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}