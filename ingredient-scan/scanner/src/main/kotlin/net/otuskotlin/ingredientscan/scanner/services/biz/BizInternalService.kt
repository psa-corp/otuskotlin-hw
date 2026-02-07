package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponse
import net.otuskotlin.ingredientscan.app.internal.IsInternalAppSettings
import net.otuskotlin.ingredientscan.app.internal.submitHelper
import net.otuskotlin.ingredientscan.biz.common.IsBizInternalProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryAnalysisRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class BizInternalService(
    private val compositionRepository: InMemoryCompositionRepository,
    private val analysisRepository: InMemoryAnalysisRepository,
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
        return appSettings.submitHelper(request, this::class, operation)
    }

}