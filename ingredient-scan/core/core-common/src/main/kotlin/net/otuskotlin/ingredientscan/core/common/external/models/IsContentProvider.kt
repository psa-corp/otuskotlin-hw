package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.IsContext

interface IsContentProvider {
    suspend fun upload(context: IsContext, files: Any, prefix: String?): List<String>
    suspend fun download(context: IsContext, fileName: String) : Any
}
