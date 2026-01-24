package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.biz.common.general.initStatus
import net.otuskotlin.ingredientscan.biz.common.general.operation
import net.otuskotlin.ingredientscan.biz.common.repo.*
import net.otuskotlin.ingredientscan.biz.common.sub.prepareToSubProcessor
import net.otuskotlin.ingredientscan.biz.common.validation.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanType
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.core.cor.chain
import net.otuskotlin.ingredientscan.core.cor.rootChain
import net.otuskotlin.ingredientscan.core.cor.worker


class IsBizProcessor(private val settings: IsCorSettings) {
    var migrationCommand: List<IsCommand> = arrayListOf(
        IsCommand.COMPOSITION_GET,
        IsCommand.COMPOSITION_CONTEXT_GET,
        IsCommand.COMPOSITION_CREATE_MANUAL,
        IsCommand.COMPOSITION_CREATE_PHOTOS,
        IsCommand.ANALYSIS_GET,
        IsCommand.ANALYSIS_CREATE,
        IsCommand.ANALYSIS_REGENERATE
    )

    suspend fun exec(context: IsContext) {

        if (migrationCommand.contains(context.command)) {
            execCor(context)
            return
        }

        when (context.command) {
            IsCommand.ANALYSIS_GET,
            IsCommand.ANALYSIS_CREATE,
            IsCommand.ANALYSIS_REGENERATE -> context.analysisResponse = STUB_ANALYSIS
            IsCommand.COMPOSITION_GET -> context.compositionResponse = STUB_COMPOSITION
            IsCommand.COMPOSITION_CONTEXT_GET,
            IsCommand.COMPOSITION_CREATE_MANUAL,
            IsCommand.COMPOSITION_CREATE_PHOTOS -> {
                context.compositionContextResponse = STUB_COMPOSITION_CONTEXT_FINISHING
                context.id = STUB_COMPOSITION_CONTEXT_FINISHING.id
            }

            else -> {}
        }

        context.state = IsState.FINISHING
        settings.contextRepository?.save(context)

    }

    suspend fun execCor(context: IsContext) = businessChain.exec(context.also { it.settings = settings })

    private val businessChain = rootChain<IsContext> {
        initStatus("Инициализация статуса процессора")
        initRepoComposition("Инициализация репозитория состава")
        initRepoContext("Инициализация репозитория контекста")
        initRepoAnalysis("Инициализация репозитория анализа")

        operation("Получение состава по ID", IsCommand.COMPOSITION_GET) {
            validation {
                worker("Копируем composition id в validateCompositionId") { validateCompositionId = compositionIdRequest }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")

                finishValidationCompositionId("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                repoReadComposition("Чтение состава из БД")
            }
            prepareResult("Подготовка ответа")
            repoSaveContext("Сохранение контекста в БД")
        }
        operation("Получение контекста состава по ID", IsCommand.COMPOSITION_CONTEXT_GET) {
            validation {
                worker("Копируем context id в validateContextId") { validateContextId = contextIdRequest }
                validateIdNotEmptyContext("Проверка, что заголовок не пуст")
                validateIdProperFormatContext("Проверка формата id", "context")

                finishValidationContext("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                repoReadContext("Чтение состава из БД")
            }
            prepareResult("Подготовка ответа")
            repoSaveContext("Сохранение контекста в БД")
        }
        operation("Создание состава по руками заполненному тексту", IsCommand.COMPOSITION_CREATE_MANUAL) {
            validation {
                worker("Копируем scan request в validateScan") { validateScan = scanRequest }
                validateScanType("Проверка scan type", arrayListOf(IsScanType.MANUAL))
                validateTextNotEmptyScan("Проверка, что текст не пуст")

                finishValidationScan("Завершение проверок")
            }
            chain {
                title = "Логика подготовки состава к пост процессингу"
                prepareToSubProcessor("Подготовка данных для пост процесса", IsSubCommand.COMPOSITION_CREATE)
                worker("Копируем scan request в validateScan") { scan = validatedScan }
            }
            repoSaveContext("Сохранение контекста в БД")
        }
        operation("Создание состава по фото", IsCommand.COMPOSITION_CREATE_PHOTOS) {
            validation {
                worker("Копируем scan request в validateScan") { validateScan = scanRequest }
                validateScanType("Проверка scan type", arrayListOf(IsScanType.PHOTO))
                validateFilesScan("Проверка, что список файлов не пуст")


                finishValidationScan("Завершение проверок")
            }
            chain {
                title = "Логика подготовки состава к пост процессингу"
                prepareToSubProcessor("Подготовка данных для пост процесса", IsSubCommand.OCR_RECOGNITION)
            }
            repoSaveContext("Сохранение контекста в БД")
        }
        operation("Получение анализа по ID", IsCommand.ANALYSIS_GET) {
            validation {
                worker("Копируем analysis id в validateAnalysisId") { validateAnalysisId = analysisIdRequest }
                validateIdNotEmptyAnalysis("Проверка, что заголовок не пуст")
                validateIdProperFormatAnalysis("Проверка формата id", "analysis")

                finishValidationAnalysis("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                repoReadAnalysis("Чтение состава из БД")
                prepareToSubProcessor("Подготовка данных для пост процесса", IsSubCommand.ANALYSIS_REGENERATE)
            }
            repoSaveContext("Сохранение контекста в БД")
        }
        operation("Создание анализа по id состава", IsCommand.ANALYSIS_CREATE) {
            validation {
                worker("Копируем composition id в validateCompositionId") { validateCompositionId = compositionIdRequest }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")

                finishValidationScan("Завершение проверок")
            }
            chain {
                title = "Логика подготовки анализа к созданию в пост процессинге"
                prepareToSubProcessor("Подготовка данных для пост процесса", IsSubCommand.ANALYSIS_CREATE)
            }
            repoSaveContext("Сохранение контекста в БД")
        }
    }.build()

}