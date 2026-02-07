package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.biz.common.general.initStatus
import net.otuskotlin.ingredientscan.biz.common.general.operation
import net.otuskotlin.ingredientscan.biz.common.repo.initRepoAnalysis
import net.otuskotlin.ingredientscan.biz.common.repo.initRepoComposition
import net.otuskotlin.ingredientscan.biz.common.repo.prepareResult
import net.otuskotlin.ingredientscan.biz.common.repo.repoReadAnalysisByComposition
import net.otuskotlin.ingredientscan.biz.common.repo.repoReadCompositionByText
import net.otuskotlin.ingredientscan.biz.common.repo.repoSaveAnalysis
import net.otuskotlin.ingredientscan.biz.common.repo.repoSaveComposition
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.core.cor.chain
import net.otuskotlin.ingredientscan.core.cor.rootChain
import net.otuskotlin.ingredientscan.core.cor.worker


class IsBizInternalProcessor(private val settings: IsCorSettings) {

    suspend fun exec(context: InternalContext) = businessChain.exec(context.also { it.settings = settings })

    private val businessChain = rootChain<InternalContext> {
        initStatus("Инициализация статуса процессора")
        initRepoComposition("Инициализация репозитория состава")
        initRepoAnalysis("Инициализация репозитория анализа")

        operation("Попоиск анализа по ID состава", InternalCommand.ANALYSIS_FIND) {
            chain {
                title = "Логика чтения"
                repoReadAnalysisByComposition("Чтение состава из БД")
            }
            prepareResult("Подготовка ответа")
        }
        operation("Сохранение анализа в БД", InternalCommand.ANALYSIS_SAVE) {
            chain {
                title = "Логика чтения"
                repoSaveAnalysis("Сохранение анализа в БД")
                worker("Заполняем ответ") { analysisResponse = analysisRequest }
            }
            prepareResult("Подготовка ответа")
        }
        operation("Попоиск состава тексту", InternalCommand.COMPOSITION_FIND) {
            chain {
                title = "Логика чтения"
                repoReadCompositionByText("Чтение состава из БД")
            }
            prepareResult("Подготовка ответа")
        }
        operation("Сохранение анализа в БД", InternalCommand.COMPOSITION_SAVE) {
            chain {
                title = "Логика чтения"
                repoSaveComposition("Сохранение анализа в БД")
                worker("Заполняем ответ") { compositionResponse = compositionRequest }
            }
            prepareResult("Подготовка ответа")
        }
    }.build()

}