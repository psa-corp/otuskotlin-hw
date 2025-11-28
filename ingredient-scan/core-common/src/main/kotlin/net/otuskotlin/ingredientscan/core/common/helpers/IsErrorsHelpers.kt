package net.otuskotlin.ingredientscan.core.common.helpers

import net.otuskotlin.ingredientscan.core.common.models.IsError

fun Throwable.asIsError(
    code: String = "unknown",
    group: String = "exceptions",
    message: String = this.message ?: "",
) = IsError(
    code = code,
    group = group,
    field = "",
    message = message,
    exception = this,
)