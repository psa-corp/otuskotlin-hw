package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContextRepository {
    suspend fun save(context: IsContext): IsContext
    suspend fun findById(id: String): IsContext?
    suspend fun delete(key: String)
    suspend fun clear()

    companion object {
        val NONE = object : IsContextRepository {
            override suspend fun save(context: IsContext): IsContext {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun findById(id: String): IsContext? {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun delete(key: String) {
                throw NotImplementedError("Must not be used")
            }

            override suspend fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}