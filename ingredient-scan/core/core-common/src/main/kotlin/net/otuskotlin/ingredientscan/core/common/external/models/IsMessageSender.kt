package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsMessageSender {
    suspend fun send(context: IsContext)

    companion object {
        val NONE = object : IsMessageSender {
            override suspend fun send(context: IsContext) {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}