package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContextRepository {
    fun save(context: IsContext): IsContext
    fun findById(id: String): IsContext?
    fun delete(key: String)
    fun clear()
}