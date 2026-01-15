package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.Error as ExternalError
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.mappers.v1.models.DownloadErrorResponse

fun IsContext.toDownloadFileErrorResponse(): DownloadErrorResponse {
    return DownloadErrorResponse(
        responseType = "downloadError",
        result = this.state.toDownloadFileResult(),
        errors = this.errors.map { error ->
            ExternalError(
                code = error.code.takeIf { it.isNotBlank() },
                group = error.group.takeIf { it.isNotBlank() },
                field = error.field.takeIf { it.isNotBlank() },
                message = error.message.takeIf { it.isNotBlank() }
            )
        }
    )
}

private fun IsState.toDownloadFileResult(): ResponseResult {
    return when (this) {
        IsState.RUNNING, IsState.FINISHING -> ResponseResult.SUCCESS
        IsState.FAILING, IsState.NONE -> ResponseResult.ERROR
    }
}