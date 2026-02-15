package net.otuskotlin.ingredientscan.core.common.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsComponent
import com.fasterxml.jackson.module.kotlin.readValue

val commonContextMapper: JsonMapper = JsonMapper.builder().run {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    addModule(KotlinModule.Builder().build())
    enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
    addModule(JavaTimeModule())
    build()
}

@Suppress("unused")
fun commonContextSerialize(request: IsContext): String = commonContextMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun commonContextDeserialize(json: String): IsContext =
    commonContextMapper.readValue(json, IsContext::class.java) as IsContext


@Suppress("unused")
fun commonLightContextSerialize(request: IsLightContext): String = commonContextMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun commonLightContextDeserialize(json: String): IsLightContext =
    commonContextMapper.readValue(json, IsLightContext::class.java) as IsLightContext

@Suppress("unused")
fun commonListComponentsSerialize(request: List<IsComponent>): String = commonContextMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun commonListComponentsDeserialize(json: String): List<IsComponent> =
    commonContextMapper.readValue(json)