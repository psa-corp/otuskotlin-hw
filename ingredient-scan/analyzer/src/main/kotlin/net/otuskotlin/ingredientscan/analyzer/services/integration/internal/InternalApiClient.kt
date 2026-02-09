package net.otuskotlin.ingredientscan.analyzer.services.integration.internal

import net.otuskotlin.ingredientscan.api.v1.internal.models.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class InternalApiClient(
    @Qualifier("internalWebClient") private val webClient: WebClient
) {
    companion object {
        const val INTERNAL_ANALYSIS_FIND: String = "/internal/analysis/find"
        const val INTERNAL_ANALYSIS_SAVE: String = "/internal/analysis/save"
        const val INTERNAL_COMPOSITION_FIND: String = "/internal/composition/find"
        const val INTERNAL_COMPOSITION_SAVE: String = "/internal/composition/save"
    }

    suspend fun internalAnalysisFind(request: InternalAnalysisFindRequest): InternalAnalysisFindResponse {
        return webClient.post()
            .uri(INTERNAL_ANALYSIS_FIND)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody() // Suspend-функция, не блокирует поток
    }

    suspend fun internalAnalysisSave(request: InternalAnalysisSaveRequest): InternalAnalysisSaveResponse {
        return webClient.post()
            .uri(INTERNAL_ANALYSIS_SAVE)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

    suspend fun internalCompositionFind(request: InternalCompositionFindRequest): InternalCompositionFindResponse {
        return webClient.post()
            .uri(INTERNAL_COMPOSITION_FIND)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

    suspend fun internalCompositionSave(request: InternalCompositionSaveRequest): InternalCompositionSaveResponse {
        return webClient.post()
            .uri(INTERNAL_COMPOSITION_SAVE)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }
}
