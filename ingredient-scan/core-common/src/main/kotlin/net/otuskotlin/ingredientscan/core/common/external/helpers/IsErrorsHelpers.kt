package net.otuskotlin.ingredientscan.core.common.external.helpers

import net.otuskotlin.ingredientscan.core.common.external.models.IsError

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