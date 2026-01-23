package net.otuskotlin.ingredientscan.core.common.external.models

interface IsCompositionRepository {
    suspend fun saveComposition(composition: IsComposition)
    suspend fun findCompositionById(id: IsCompositionId): IsComposition?
    suspend fun findCompositionByText(text: String): IsComposition?
    suspend fun deleteComposition(id: IsCompositionId)
    suspend fun clearCompositions()

    companion object {
        val NONE = object : IsCompositionRepository {
            override suspend fun saveComposition(composition: IsComposition) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findCompositionById(id: IsCompositionId): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findCompositionByText(text: String): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun deleteComposition(id: IsCompositionId) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clearCompositions() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}