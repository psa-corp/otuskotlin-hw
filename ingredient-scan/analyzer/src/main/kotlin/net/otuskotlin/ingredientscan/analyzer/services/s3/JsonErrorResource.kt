package net.otuskotlin.ingredientscan.analyzer.services.s3

import net.otuskotlin.ingredientscan.api.v1.external.apiV1ExternalResponseSerialize
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import org.springframework.core.io.AbstractResource
import java.io.ByteArrayInputStream
import java.io.InputStream

class JsonErrorResource(private val response: IResponse) :
    AbstractResource() {

    private val jsonBytes by lazy {
        apiV1ExternalResponseSerialize(response).toByteArray(Charsets.UTF_8)
    }

    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(jsonBytes)
    }

    override fun contentLength(): Long {
        return jsonBytes.size.toLong()
    }

    override fun exists(): Boolean {
        return true
    }

    override fun getDescription(): String {
        return "JSON error response for download"
    }
}