package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.logging.IsLogLevel

data class IsError(
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val level: IsLogLevel = IsLogLevel.ERROR,
    val exception: Throwable? = null,
)