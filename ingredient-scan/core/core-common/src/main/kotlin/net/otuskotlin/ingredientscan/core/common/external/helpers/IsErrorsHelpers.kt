package net.otuskotlin.ingredientscan.core.common.external.helpers


import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.logging.IsLogLevel

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


inline fun IsContext.addError(error: IsError) = errors.add(error)
inline fun IsContext.addErrors(error: Collection<IsError>) = errors.addAll(error)

inline fun IsContext.fail(error: IsError) {
    addError(error)
    state = IsState.FAILING
}

inline fun IsContext.fail(errors: Collection<IsError>) {
    addErrors(errors)
    state = IsState.FAILING
}

inline fun errorValidation(
    field: String,
    /**
     * Код, характеризующий ошибку. Не должен включать имя поля или указание на валидацию.
     * Например: empty, badSymbols, tooLong, etc
     */
    violationCode: String,
    description: String,
    level: IsLogLevel = IsLogLevel.ERROR,
) = IsError(
    code = "validation-$field-$violationCode",
    field = field,
    group = "validation",
    message = "Validation error for field $field: $description",
    level = level,
)

inline fun errorSystem(
    violationCode: String,
    level: IsLogLevel = IsLogLevel.ERROR,
    e: Throwable,
) = IsError(
    code = "system-$violationCode",
    group = "system",
    message = "System error occurred. Our stuff has been informed, please retry later",
    level = level,
    exception = e,
)

inline fun errorRepo(
    field: String,
    violationCode: String,
    description: String,
    level: IsLogLevel = IsLogLevel.ERROR,
    e: Throwable,
) = IsError(
    code = "repo-$violationCode",
    group = "repository",
    message = "Repository error for field $field: $description",
    level = level,
    exception = e,
)

