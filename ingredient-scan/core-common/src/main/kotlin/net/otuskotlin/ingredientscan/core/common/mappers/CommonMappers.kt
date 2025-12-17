package net.otuskotlin.ingredientscan.core.common.mappers

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.otuskotlin.ingredientscan.core.common.external.IsContext

val commonContextMapper: JsonMapper = JsonMapper.builder().run {
    addModule(KotlinModule.Builder().build())
    enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
    addModule(JavaTimeModule())
    build()
}

@Suppress("unused")
fun commonContextSerialize(request: IsContext): String = commonContextMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun apiContextDeserialize(json: String): IsContext =
    commonContextMapper.readValue(json, IsContext::class.java) as IsContext