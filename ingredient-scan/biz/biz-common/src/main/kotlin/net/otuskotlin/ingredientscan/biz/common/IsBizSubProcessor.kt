package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.biz.common.general.initStatus
import net.otuskotlin.ingredientscan.biz.common.general.subOperation
import net.otuskotlin.ingredientscan.biz.common.repo.initRepoContext
import net.otuskotlin.ingredientscan.biz.common.repo.prepareResult
import net.otuskotlin.ingredientscan.biz.common.repo.repoSaveContext
import net.otuskotlin.ingredientscan.biz.common.sub.awaitContext
import net.otuskotlin.ingredientscan.biz.common.sub.initContextAwaitService
import net.otuskotlin.ingredientscan.biz.common.sub.initMessageSender
import net.otuskotlin.ingredientscan.biz.common.sub.sendContext
import net.otuskotlin.ingredientscan.biz.common.validation.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanType
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.cor.chain
import net.otuskotlin.ingredientscan.core.cor.rootChain
import net.otuskotlin.ingredientscan.core.cor.worker

class IsBizSubProcessor(private val settings: IsCorSettings) {
    suspend fun exec(context: IsContext) {
        if (!context.errors.isEmpty()) {
            return
        }
        execCor(context)
    }

    suspend fun execCor(context: IsContext) = businessChain.exec(context.also { it.settings = settings })

    private val businessChain = rootChain<IsContext> {
        initStatus("Инициализация статуса пост процессора")
        initRepoContext("Инициализация репозитория контекста")
        initMessageSender("Инициализация отправителя сообщений в очередь")
        initContextAwaitService("Инициализация ожидающего сервиса")

        subOperation("Создание состава на основе заполненного скана", IsSubCommand.COMPOSITION_CREATE) {
            validation {
                worker("Копируем scan request в validateScan") { validateScan = scan }
                validateScanType("Проверка scan type", arrayListOf(IsScanType.MANUAL, IsScanType.PHOTO))
                validateTextNotEmptyScan("Проверка, что текст не пуст")

                finishValidationScan("Завершение проверок")
            }
            chain {
                title = "Логика создания состава"
                worker("Копируем scan request в validateScan") { scan = validatedScan }
                repoSaveContext("Сохранение контекста в БД")
                sendContext("Отправляем сообщение для ИИ валидации текста и создания состава")
                awaitContext("Ожидание выполнения задачи", 300_000)
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.COMPOSITION_VALIDATE }
                repoSaveContext("Сохранение контекста в БД")
            }
        }
        subOperation("Создание состава по фото скана", IsSubCommand.OCR_RECOGNITION) {
            validation {
                worker("Копируем scan request в validateScan") { validateScan = scan }
                validateScanType("Проверка scan type", arrayListOf(IsScanType.MANUAL, IsScanType.PHOTO))
                validateFilesNotEmptyScan("Проверка, что есть файлы с фото")

                finishValidationScan("Завершение проверок")
            }
            chain {
                title = "Логика создания состава"
                worker("Копируем validateScan в scan") { scan = validatedScan }
                repoSaveContext("Сохранение контекста в БД")
                sendContext("Отправляем сообщение для получения текста из фото, ИИ валидации текста и создания состава")
                awaitContext("Ожидание выполнения задачи", 300_000)
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.COMPOSITION_VALIDATE }
                repoSaveContext("Сохранение контекста в БД")
            }
        }
        subOperation("Валидация состава перед ответом", IsSubCommand.COMPOSITION_VALIDATE) {
            validation {
                worker("Копируем состав response в validateComposition") { validateComposition = composition }
                validateIdContext("Проверяем что разморозили правильный поток")
                validateTextNotEmptyComposition("Проверка, что текст не пуст")

                worker("Копируем composition id в validateCompositionId") {
                    validateCompositionId = validateComposition.id
                }
                validateIdNotEmptyComposition("Проверка, что ID не пуст")

                finishValidationComposition("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                worker("Копируем состав после валидации в ответ") { compositionResponse = validatedComposition }
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.READY }
                repoSaveContext("Сохранение контекста в БД")
            }
            prepareResult("Подготовка ответа")
        }
        subOperation("Создание анализа на состава", IsSubCommand.ANALYSIS_CREATE) {
            validation {
                worker("Копируем composition в validateComposition") { validateComposition = composition }
                worker("Копируем composition id в validateCompositionId") { validateCompositionId = validateComposition.id }
                worker("Стираем analysisResponse") { analysisResponse = IsAnalysis.NONE }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")
                validateTextNotEmptyComposition("Проверка, что текст не пуст")

                finishValidationComposition("Завершение проверок")
            }
            chain {
                title = "Логика создания состава"
                worker("Копируем validatedComposition в composition") { composition = validatedComposition }
                worker("Стираем analysis") { analysis = IsAnalysis.NONE }
                repoSaveContext("Сохранение контекста в БД")
                sendContext("Отправляем сообщение для создания анализа")
                awaitContext("Ожидание выполнения задачи", 300_000)
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.ANALYSIS_VALIDATE }
                repoSaveContext("Сохранение контекста в БД")
            }
        }
        subOperation("Создание анализа на состава", IsSubCommand.ANALYSIS_REGENERATE) {
            validation {
                worker("Копируем composition в validateComposition") { validateComposition = composition }
                worker("Копируем composition id в validateCompositionId") { validateCompositionId = validateComposition.id }
                worker("Копируем анализ для валидации") { validateAnalysis = analysis }
                worker("Копируем анализ id в validateAnalysisId") { validateAnalysisId = validateAnalysis.id }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")
                validateIdNotEmptyAnalysis("Проверка, что заголовок не пуст")
                validateIdProperFormatAnalysis("Проверка формата id", "analysis")

                finishValidationAnalysis("Завершение проверок")
                finishValidationComposition("Завершение проверок")
            }
            chain {
                title = "Логика создания состава"
                worker("Копируем validatedAnalysis в analysis") { analysis = validatedAnalysis }
                worker("Копируем composition в validatedComposition") { composition = validatedComposition }
                worker("Стираем analysisResponse") { analysisResponse = IsAnalysis.NONE }
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.ANALYSIS_CREATE }
                repoSaveContext("Сохранение контекста в БД")
                sendContext("Отправляем сообщение для создания анализа")
                awaitContext("Ожидание выполнения задачи", 300_000)
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.ANALYSIS_VALIDATE }
                repoSaveContext("Сохранение контекста в БД")
            }
        }
        subOperation("Валидация анализа перед ответом", IsSubCommand.ANALYSIS_VALIDATE) {
            validation {
                worker("Копируем состав response в validateComposition") { validateAnalysis = analysis }
                worker("Копируем id состава для валидации") { validateCompositionId = validateAnalysis.compositionId }
                worker("Копируем id состава для валидации") { validateAnalysisId = validateAnalysis.id }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")
                validateIdNotEmptyAnalysis("Проверка, что заголовок не пуст")
                validateIdProperFormatAnalysis("Проверка формата id", "analysis")

                validateDescriptionNotEmptyAnalysis("Проверка, что текст описания не пуст")
                validateColorNotEmptyAnalysis("Проверка, что цвет не пуст")
                validateHasRatingAnalysis("Проверка, что есть рейтинг")
                finishValidationAnalysis("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                worker("Копируем состав после валидации в ответ") { analysisResponse = validatedAnalysis }
                repoSaveContext("Сохранение контекста в БД")
            }
            prepareResult("Подготовка ответа")
        }
    }.build()


}