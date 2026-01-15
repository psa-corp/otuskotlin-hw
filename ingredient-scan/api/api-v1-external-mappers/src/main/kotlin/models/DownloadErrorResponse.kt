package net.otuskotlin.ingredientscan.mappers.v1.models

import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import net.otuskotlin.ingredientscan.api.v1.external.models.ResponseResult
import net.otuskotlin.ingredientscan.api.v1.external.models.Error

data class DownloadErrorResponse(
    override val responseType: String = "downloadError",
    override val result: ResponseResult = ResponseResult.ERROR,
    override val errors: List<Error>? = emptyList()
) : IResponse {}