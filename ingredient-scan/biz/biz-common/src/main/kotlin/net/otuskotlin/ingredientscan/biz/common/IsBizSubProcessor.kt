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

//        when (context.subCommand){
//            IsSubCommand.COMPOSITION_CREATE, IsSubCommand.OCR_RECOGNITION, IsSubCommand.ANALYSIS_REGENERATE -> {
//                settings.messageSender?.send(context)
//            }
//            else -> {}
//        }
//        settings.contextRepository?.save(context)
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
                worker("Копируем scan request в validateScan") { context?.let { compositionResponse = it.compositionResponse } }
                worker("Отправляем на валидацию перед ответом") { subCommand = IsSubCommand.COMPOSITION_VALIDATE}
            }
        }
        subOperation("Валидация состава перед ответом", IsSubCommand.COMPOSITION_VALIDATE) {
            validation {
                worker("Копируем состав response в validateComposition") { validateComposition = compositionResponse }
                validateTextNotEmptyComposition("Проверка, что текст не пуст")

                worker("Копируем composition id в validateCompositionId") { validateCompositionId = validateComposition.id }
                validateIdNotEmptyComposition("Проверка, что ID не пуст")

                finishValidationCompositionId("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                worker("Копируем состав после валидации в ответ") { compositionResponse = validatedComposition }
                repoSaveContext("Сохранение контекста в БД")
            }
            prepareResult("Подготовка ответа")
        }
    }.build()


}