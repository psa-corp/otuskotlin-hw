package net.otuskotlin.ingredientscan.analyzer.repositories

import net.otuskotlin.ingredientscan.analyzer.services.integration.internal.InternalApiClient
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisFindRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalAnalysisSaveRequest
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternal
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternalTransport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("webAnalysisRepo")
class WebAnalysisRepository(
    private val internalApiClient: InternalApiClient
) : IsAnalysisRepository {
    
    private val log = LoggerFactory.getLogger(WebAnalysisRepository::class.java)
    override suspend fun saveAnalysis(analysis: IsAnalysis) {
        log.info("(Integration) Analysis save: {}", analysis)
         val a = analysis.toInternalTransport()
             ?: throw NullPointerException("(Integration) analysis.toInternalTransport() returned null.")

         val response = internalApiClient.internalAnalysisSave(
            InternalAnalysisSaveRequest(
                analysis = a,
                requestType = "internalAnalysisSave",
            )
        )
        response.analysis?.toInternal()
            ?: throw NullPointerException("(Integration) Save analysis returned null.")
    }

    override suspend fun findAnalysisById(id: IsAnalysisId): IsAnalysis? {
        throw NotImplementedError("Must not be used")
    }

    override suspend fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis? {
        log.info("(Integration) Analysis find by composition id:{}", id.asString())
        val response = internalApiClient.internalAnalysisFind(
            InternalAnalysisFindRequest(
                compositionId = id.asString(),
                requestType = "internalAnalysisFind",
            )
        )
        return response.analysis?.toInternal()
    }

    override suspend fun updateAnalysis(analysis: IsAnalysis) {
        throw NotImplementedError("Must not be used")
    }

    override suspend fun deleteAnalysis(id: IsAnalysisId) {
        throw NotImplementedError("Must not be used")
    }

    override suspend fun clearAnalysis() {
        throw NotImplementedError("Must not be used")
    }

}