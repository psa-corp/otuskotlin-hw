package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext

interface IsLightContextRepository {
    fun save(context: IsLightContext)
    fun findById(id: IsContextId): IsLightContext?
    fun delete(id: IsContextId)
    fun clear()


    companion object {
        val NONE = object : IsLightContextRepository {
            override fun save(context: IsLightContext) {
                throw NotImplementedError("Must not be used")
            }

            override fun findById(id: IsContextId): IsLightContext? {
                throw NotImplementedError("Must not be used")
            }

            override fun delete(id: IsContextId) {
                throw NotImplementedError("Must not be used")
            }

            override fun clear() {
                throw NotImplementedError("Must not be used")
            }
        }
    }
}