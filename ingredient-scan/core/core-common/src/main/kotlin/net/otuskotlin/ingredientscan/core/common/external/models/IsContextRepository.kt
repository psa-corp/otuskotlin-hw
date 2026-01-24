package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContextRepository {
    suspend fun save(context: IsContext)
    suspend fun findById(id: IsContextId): IsContext?
    suspend fun delete(id: IsContextId)
    suspend fun clear()

    fun saveUnsuspend(context: IsContext)

    companion object {
        val NONE = object : IsContextRepository {
            override suspend fun save(context: IsContext) {
                throw NotImplementedError("Must not be used")
            }

            override fun saveUnsuspend(context: IsContext) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findById(id: IsContextId): IsContext? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun delete(id: IsContextId) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}