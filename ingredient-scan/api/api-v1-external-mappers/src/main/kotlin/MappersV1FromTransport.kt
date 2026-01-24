package net.otuskotlin.ingredientscan.mappers.v1

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsScan
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanId
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanType
import net.otuskotlin.ingredientscan.core.common.external.models.IsWorkMode
import net.otuskotlin.ingredientscan.core.common.external.IsStubs
import net.otuskotlin.ingredientscan.mappers.v1.exceptions.UnknownRequestClass
import java.util.UUID.randomUUID


// --- Analysis Mappers ---

fun IsContext.fromTransport(request: AnalysisGetRequest) {
    command = IsCommand.ANALYSIS_GET

    analysisIdRequest = request.analysisId.toAnalysisId()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: AnalysisRegenerateRequest) {
    command = IsCommand.ANALYSIS_REGENERATE

    analysisIdRequest = request.analysisId.toAnalysisId()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

// --- Composition Mappers ---

fun IsContext.fromTransport(request: CompositionCreateByManualRequest) {
    command = IsCommand.COMPOSITION_CREATE_MANUAL

    // Маппим входящие данные сканирования
    scanRequest = request.scan.toInternal()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: CompositionCreateByPhotosRequest, photos: MutableList<String>) {
    command = IsCommand.COMPOSITION_CREATE_PHOTOS

    scanRequest = request.scan?.toInternal(photos) ?: IsScan()

    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: CompositionGetRequest) {
    command = IsCommand.COMPOSITION_GET
    compositionIdRequest = IsCompositionId(request.compositionId.toString())
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: CompositionContextGetRequest) {
    command = IsCommand.COMPOSITION_CONTEXT_GET
    contextIdRequest = IsContextId(request.contextId.toString())
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

// --- File/Download Mappers ---

fun IsContext.fromTransport(request: DownloadFileRequest) {
    command = IsCommand.DOWNLOAD_FILE
    workMode = request.debug.transportToWorkMode()
    stubCase = request.debug.transportToStubCase()
}

fun IsContext.fromTransport(request: IRequest, photos: MutableList<String>) = when (request) {
    is CompositionCreateByPhotosRequest -> fromTransport(request, photos)
    else -> throw UnknownRequestClass(request.javaClass)
}

fun IsContext.fromTransport(request: IRequest) = when (request) {
    is AnalysisGetRequest -> fromTransport(request)
    is AnalysisRegenerateRequest -> fromTransport(request)
    is CompositionCreateByManualRequest -> fromTransport(request)
    is CompositionContextGetRequest -> fromTransport(request)
    is CompositionGetRequest -> fromTransport(request)
    is DownloadFileRequest -> fromTransport(request)
    else -> throw UnknownRequestClass(request.javaClass)
}

// --- Helpers ---

fun ScanManualDto.toInternal(): IsScan = IsScan(
    id = IsScanId("scan-${randomUUID()}"),
    text = this.text,
    type = this.type.toInternal()
)

fun ScanPhotosDto.toInternal(photos : MutableList<String>): IsScan = IsScan(
    id = IsScanId("scan-${randomUUID()}"),
    files = photos,
    type = this.type.toInternal()
)

fun String?.toScanId() = this?.let { IsScanId(it) } ?: IsScanId.NONE

fun RequestDebug?.transportToWorkMode(): IsWorkMode = when (this?.mode) {
    DebugMode.PROD -> IsWorkMode.PROD
    DebugMode.TEST -> IsWorkMode.TEST
    DebugMode.STUB -> IsWorkMode.STUB
    null -> IsWorkMode.PROD
}

fun RequestDebug?.transportToStubCase(): IsStubs = when (this?.stub) {
    RequestDebugStub.SUCCESS -> IsStubs.SUCCESS
    RequestDebugStub.NOT_FOUND -> IsStubs.NOT_FOUND
    RequestDebugStub.BAD_ID -> IsStubs.BAD_ID
    else -> IsStubs.NONE
}

fun ScanType?.toInternal(): IsScanType = when (this) {
    ScanType.MANUAL -> IsScanType.MANUAL
    ScanType.PHOTO -> IsScanType.PHOTO
    null -> IsScanType.NONE
}


fun String?.toAnalysisId() = this?.let { IsAnalysisId(it) } ?: IsAnalysisId.NONE