package net.otuskotlin.ingredientscan.analyzer.services.integration.ai

import net.otuskotlin.ingredientscan.core.common.ai.AICompositionRequest
import net.otuskotlin.ingredientscan.core.common.ai.AiAnalysis
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AIApiClient(
    @Qualifier("aiWebClient") private val webClient: WebClient
) {
    companion object {
        const val AI_ANALYSIS_CREATE: String = "/ai/analyze"
    }

    suspend fun aiAnalyzeCreate(request: AICompositionRequest): AiAnalysis {
        return webClient.post()
            .uri(AI_ANALYSIS_CREATE)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

//    suspend fun internalAnalysisSave(request: InternalAnalysisSaveRequest): InternalAnalysisSaveResponse {
//        return webClient.post()
//            .uri(INTERNAL_ANALYSIS_SAVE)
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(request)
//            .retrieve()
//            .awaitBody()
//    }
//
//    suspend fun internalCompositionFind(request: InternalCompositionFindRequest): InternalCompositionFindResponse {
//        return webClient.post()
//            .uri(INTERNAL_COMPOSITION_FIND)
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(request)
//            .retrieve()
//            .awaitBody()
//    }
//
//    suspend fun internalCompositionSave(request: InternalCompositionSaveRequest): InternalCompositionSaveResponse {
//        return webClient.post()
//            .uri(INTERNAL_COMPOSITION_SAVE)
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(request)
//            .retrieve()
//            .awaitBody()
//    }
}
