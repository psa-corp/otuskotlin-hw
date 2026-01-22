package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContextRepository {
    suspend fun save(context: IsContext): IsContext
    suspend fun findById(id: IsContextId): IsContext?
    suspend fun delete(key: IsContextId)
    suspend fun clear()

    companion object {
        val NONE = object : IsContextRepository {
            override suspend fun save(context: IsContext): IsContext {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findById(id: IsContextId): IsContext? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun delete(key: IsContextId) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}