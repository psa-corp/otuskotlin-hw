package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.api.v1.external.models.IRequest
import net.otuskotlin.ingredientscan.api.v1.external.models.IResponse
import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.app.common.submitHelper
import net.otuskotlin.ingredientscan.app.content.downloadHelper
import net.otuskotlin.ingredientscan.app.content.uploadHelper
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.scanner.services.await.ContextAwaitService
import net.otuskotlin.ingredientscan.scanner.services.s3.S3CloudService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
open class BizService(
    private val kafkaSender: BizKafkaSender,
    @Qualifier("memoryCompositionRepo") private val compositionRepository: IsCompositionRepository,
    @Qualifier("memoryContextRepo") private val contextRepository: IsContextRepository,
    @Qualifier("memoryAnalysisRepo") private val analysisRepository: IsAnalysisRepository,
    private val s3CloudService: S3CloudService,
    private val contextAwaitService: ContextAwaitService
) {
    private val appSettings: IsAppSettings
    private val log = LoggerFactory.getLogger(BizService::class.java)

    init {
        val settings = IsCorSettings(
            messageSender = kafkaSender,
            contentProvider = s3CloudService,
            contextRepository = contextRepository,
            compositionRepository = compositionRepository,
            analysisRepository = analysisRepository,
            contextAwaitService = contextAwaitService,
        )

        appSettings = AppSettings(
            settings = settings,
            processor = IsBizProcessor(settings),
            subProcessor = IsBizSubProcessor(settings)
        )
        log.info("BizService initialized")
    }

    open suspend fun <R : IResponse> execute(request: IRequest, operation : String) : R {
        return appSettings.submitHelper(request, this::class, operation)
    }

    open suspend fun <R : IResponse> execute(request: IRequest, photos: Flux<FilePart>, operation : String) : R {
        return appSettings.uploadHelper(request, photos,this::class, operation)
    }

    open suspend fun execute(fileName: List<String>, operation : String) : Flux<DataBuffer>  {
        return appSettings.downloadHelper(fileName, this::class, operation)
    }
}