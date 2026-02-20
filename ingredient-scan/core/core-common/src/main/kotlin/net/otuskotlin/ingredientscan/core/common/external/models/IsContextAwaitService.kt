package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext

interface IsContextAwaitService {
    suspend fun await(context: IsContext, timeout: Long) : IsLightContext

    companion object {
        val NONE = object : IsContextAwaitService {
            override suspend fun await(context: IsContext, timeout: Long) : IsLightContext {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}