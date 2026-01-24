package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContextAwaitService {
    suspend fun await(context: IsContext, timeout: Long) : IsContext

    companion object {
        val NONE = object : IsContextAwaitService {
            override suspend fun await(context: IsContext, timeout: Long) : IsContext {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}