package net.otuskotlin.ingredientscan.core.common.external.models

interface IsCompositionRepository {
    suspend fun save(composition: IsComposition)
    suspend fun findById(id: IsCompositionId): IsComposition?
    suspend fun findByText(text: String): IsComposition?
    suspend fun delete(id: IsCompositionId)
    suspend fun clear()

    fun saveUnsuspend(composition: IsComposition)
    fun findByTextUnsuspend(text: String): IsComposition?

    companion object {
        val NONE = object : IsCompositionRepository {
            override suspend fun save(composition: IsComposition) {
                throw NotImplementedError("Must not be used")
            }

            override fun saveUnsuspend(composition: IsComposition) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findById(id: IsCompositionId): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findByText(text: String): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override fun findByTextUnsuspend(text: String): IsComposition? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun delete(id: IsCompositionId) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}