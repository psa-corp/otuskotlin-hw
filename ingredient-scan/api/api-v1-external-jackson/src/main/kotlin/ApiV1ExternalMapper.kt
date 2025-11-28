package net.otuskotlin.ingredientscan.api.v1.external

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper

import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse

val apiV1ExternalMapper = JsonMapper.builder().run {
    addModule(KotlinModule.Builder().build())
    // !!!!! Включаем поддержку полиморфизма через базовый тип (IRequest/IResponse)
    enable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
    build()
}

@Suppress("unused")
fun apiV1ExternalRequestSerialize(request: IRequest): String = apiV1ExternalMapper.writeValueAsString(request)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : IRequest> apiV1ExternalRequestDeserialize(json: String): T =
    apiV1ExternalMapper.readValue(json, IRequest::class.java) as T

@Suppress("unused")
fun apiV1ExternalResponseSerialize(response: IResponse): String = apiV1ExternalMapper.writeValueAsString(response)

@Suppress("UNCHECKED_CAST", "unused")
fun <T : IResponse> apiV1ExternalResponseDeserialize(json: String): T =
    apiV1ExternalMapper.readValue(json, IResponse::class.java) as T