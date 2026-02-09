package net.otuskotlin.ingredientscan.analyzer.repositories

import net.otuskotlin.ingredientscan.analyzer.services.integration.internal.InternalApiClient
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionFindRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionSaveRequest
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternal
import net.otuskotlin.ingredientscan.mappers.v1.internal.toInternalTransport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("webCompositionRepo")
class WebCompositionRepository(
    private val internalApiClient: InternalApiClient
) : IsCompositionRepository {
    
    private val log = LoggerFactory.getLogger(WebCompositionRepository::class.java)
    override suspend fun save(composition: IsComposition) {
        log.info("(Integration) Composition save: {}", composition)
        val c = composition.toInternalTransport()
            ?: throw NullPointerException("(Integration) composition.toInternalTransport() returned null.")

        val response = internalApiClient.internalCompositionSave(
            InternalCompositionSaveRequest(
                composition = c,
                requestType = "internalCompositionSave",
            )
        )
        response.composition?.toInternal()
            ?: throw NullPointerException("(Integration) Save composition returned null.")
    }

    override suspend fun findById(id: IsCompositionId): IsComposition? {
        throw NotImplementedError("Must not be used")
    }

    override suspend fun findByText(text: String): IsComposition? {
        log.info("(Integration) Analysis find by composition id:{}", text)
        val response = internalApiClient.internalCompositionFind(
            InternalCompositionFindRequest(
                text = text,
                requestType = "internalCompositionFind",
            )
        )
        return response.composition?.toInternal()
    }

    override suspend fun delete(id: IsCompositionId) {
        throw NotImplementedError("Must not be used")
    }

    override suspend fun clear() {
        throw NotImplementedError("Must not be used")
    }

}