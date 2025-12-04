package net.otuskotlin.ingredientscan.core.common.external.models

import kotlin.jvm.JvmInline

        @JvmInline
        value class IsAnalysisId(private val id: String) {
            fun asString() = id
            companion object { val NONE = IsAnalysisId("") }
        }

@JvmInline
value class IsCompositionId(private val id: String) {
    fun asString() = id
    companion object { val NONE = IsCompositionId("") }
}

@JvmInline
value class IsComponentId(private val id: String) {
    fun asString() = id
    companion object { val NONE = IsComponentId("") }
}

@JvmInline
value class IsScanId(private val id: String) {
    fun asString() = id
    companion object { val NONE = IsScanId("") }
}

@JvmInline
value class IsUserId(private val id: String) {
    fun asString() = id
    companion object { val NONE = IsUserId("") }
}

@JvmInline
value class IsRequestId(private val id: String) {
    fun asString() = id
    companion object { val NONE = IsRequestId("") }
}