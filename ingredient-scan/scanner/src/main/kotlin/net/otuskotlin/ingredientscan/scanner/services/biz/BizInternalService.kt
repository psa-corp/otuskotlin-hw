package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponse
import net.otuskotlin.ingredientscan.app.internal.IsInternalAppSettings
import net.otuskotlin.ingredientscan.app.internal.internalSubmitHelper
import net.otuskotlin.ingredientscan.biz.common.IsBizInternalProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
open class BizInternalService(
    @Qualifier("postgresCompositionRepo") private val compositionRepository: IsCompositionRepository,
    @Qualifier("postgresAnalysisRepo") private val analysisRepository: IsAnalysisRepository,
) {
    private val appSettings: IsInternalAppSettings
    private val log = LoggerFactory.getLogger(BizInternalService::class.java)

    init {
        val settings = IsCorSettings(
            messageSender = null,
            contentProvider = null,
            contextRepository = null,
            compositionRepository = compositionRepository,
            analysisRepository = analysisRepository,
            contextAwaitService = null,
        )

        appSettings = InternalAppSettings(
            settings = settings,
            processor = IsBizInternalProcessor(settings)
        )
        log.info("BizInternalService initialized")
    }

    open suspend fun <R : InternalResponse> execute(request: InternalRequest, operation : String) : R {
        return appSettings.internalSubmitHelper(request, this::class, operation)
    }

}