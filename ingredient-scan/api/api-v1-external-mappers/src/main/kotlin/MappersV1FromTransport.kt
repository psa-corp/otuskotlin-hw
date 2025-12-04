package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsScan
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanId
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanType
import net.otuskotlin.ingredientscan.core.common.external.models.IsWorkMode
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsStubs
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownRequestClass

fun IsContext.fromTransport(request: IRequest) = when (request) {
    is AnalysisGetRequest -> fromTransport(request)
    is AnalysisRegenerateRequest -> fromTransport(request)
    is CompositionCreateByManualRequest -> fromTransport(request)
    is CompositionCreateByPhotosRequest -> fromTransport(request)
    is CompositionGetRequest -> fromTransport(request)
    is DownloadFileRequest -> fromTransport(request)
    else -> throw UnknownRequestClass(request.javaClass)
}

// --- Analysis Mappers ---

fun IsContext.fromTransport(request: AnalysisGetRequest) {
    command = IsCommand.ANALYSIS_GET

    analysisRequest.id = request.analysisId.toAnalysisId()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: AnalysisRegenerateRequest) {
    command = IsCommand.ANALYSIS_REGENERATE

    analysisRequest.id = request.analysisId.toAnalysisId()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

// --- Composition Mappers ---

fun IsContext.fromTransport(request: CompositionCreateByManualRequest) {
    command = IsCommand.COMPOSITION_CREATE_MANUAL

    // Маппим входящие данные сканирования
    scanRequest = request.scan?.toInternal() ?: IsScan()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: CompositionCreateByPhotosRequest) {
    command = IsCommand.COMPOSITION_CREATE_PHOTOS

    scanRequest = request.scan?.toInternal() ?: IsScan()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: CompositionGetRequest) {
    command = IsCommand.COMPOSITION_GET
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

// --- File/Download Mappers ---

fun IsContext.fromTransport(request: DownloadFileRequest) {
    command = IsCommand.DOWNLOAD_FILE
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

// --- Helpers ---

private fun ScanManualDto.toInternal(): IsScan = IsScan(
    id = this.id.toScanId(),
    text = this.text ?: "",
    type = this.type.toInternal()
)

private fun ScanPhotosDto.toInternal(): IsScan = IsScan(
    id = this.id.toScanId(),
    files = mutableListOf(),
    type = this.type.toInternal()
)

private fun String?.toScanId() = this?.let { IsScanId(it) } ?: IsScanId.NONE

private fun RequestDebug?.transportToWorkMode(): IsWorkMode = when (this?.mode) {
    DebugMode.PROD -> IsWorkMode.PROD
    DebugMode.TEST -> IsWorkMode.TEST
    DebugMode.STUB -> IsWorkMode.STUB
    null -> IsWorkMode.PROD
}

private fun RequestDebug?.transportToStubCase(): IsStubs = when (this?.stub) {
    RequestDebugStub.SUCCESS -> IsStubs.SUCCESS
    RequestDebugStub.NOT_FOUND -> IsStubs.NOT_FOUND
    RequestDebugStub.BAD_ID -> IsStubs.BAD_ID
    else -> IsStubs.NONE
}

private fun ScanType?.toInternal(): IsScanType = when (this) {
    ScanType.MANUAL -> IsScanType.MANUAL
    ScanType.PHOTO -> IsScanType.PHOTO
    null -> IsScanType.NONE
}


private fun String?.toAnalysisId() = this?.let { IsAnalysisId(it) } ?: IsAnalysisId.NONE