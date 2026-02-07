package net.otuskotlin.ingredientscan.api.v1.internal

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponse

val apiV1InternalMapper = JsonMapper.builder().run {
    addModule(KotlinModule.Builder().build())

    enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
    addModule(JavaTimeModule())
    build()
}

@Suppress("unused")
fun apiV1InternalRequestSerialize(request: InternalRequest): String = apiV1InternalMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : InternalRequest> apiV1InternalRequestDeserialize(json: String): T =
    apiV1InternalMapper.readValue(json, InternalRequest::class.java) as T

@Suppress("unused")
fun apiV1InternalResponseSerialize(response: InternalResponse): String = apiV1InternalMapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : InternalResponse> apiV1InternalResponseDeserialize(json: String): T =
    apiV1InternalMapper.readValue(json, InternalResponse::class.java) as T