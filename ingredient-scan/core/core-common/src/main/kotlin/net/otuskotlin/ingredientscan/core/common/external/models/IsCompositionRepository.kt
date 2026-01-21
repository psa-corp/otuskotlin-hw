package net.otuskotlin.ingredientscan.core.common.external.models

interface IsCompositionRepository {
    suspend fun save(composition: IsComposition): IsComposition
    suspend fun findById(id: IsCompositionId): IsComposition?
    suspend fun findByText(text: String): IsComposition?
    suspend fun clear()

    companion object {
        val NONE = object : IsCompositionRepository {
            override suspend fun save(composition: IsComposition): IsComposition {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findById(id: IsCompositionId): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findByText(text: String): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}