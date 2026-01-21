package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.biz.common.general.initStatus
import net.otuskotlin.ingredientscan.biz.common.general.operation
import net.otuskotlin.ingredientscan.biz.common.repo.initRepoComposition
import net.otuskotlin.ingredientscan.biz.common.repo.initRepoContext
import net.otuskotlin.ingredientscan.biz.common.repo.prepareResult
import net.otuskotlin.ingredientscan.biz.common.repo.repoReadComposition
import net.otuskotlin.ingredientscan.biz.common.repo.repoSaveContext
import net.otuskotlin.ingredientscan.biz.common.validation.finishAdValidationComposition
import net.otuskotlin.ingredientscan.biz.common.validation.validateIdNotEmptyComposition
import net.otuskotlin.ingredientscan.biz.common.validation.validateIdProperFormatComposition
import net.otuskotlin.ingredientscan.biz.common.validation.validation
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsAnalysisStub.Companion.STUB_ANALYSIS
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION
import net.otuskotlin.ingredientscan.core.common.external.stubs.IsCompositionStub.Companion.STUB_COMPOSITION_CONTEXT_FINISHING
import net.otuskotlin.ingredientscan.core.cor.chain
import net.otuskotlin.ingredientscan.core.cor.rootChain
import net.otuskotlin.ingredientscan.core.cor.worker


class IsBizProcessor(private val settings: IsCorSettings) {
    var migrationCommand: List<IsCommand> = arrayListOf(IsCommand.COMPOSITION_GET)


    suspend fun exec(context: IsContext) {

        if (migrationCommand.contains(context.command)) {
            exec1(context)
            return;
        }

        when (context.command) {
            IsCommand.ANALYSIS_GET,
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

    suspend fun exec1(context: IsContext) = businessChain.exec(context.also { it.settings = settings })

    private val businessChain = rootChain<IsContext> {
        initStatus("Инициализация статуса процессора")
        initRepoComposition("Инициализация репозитория состава")
        initRepoContext("Инициализация репозитория контекста")

        operation("Получение состава по ID", IsCommand.COMPOSITION_GET) {
            validation {
                worker("Копируем composition id в validateCompositionId") { validateCompositionId = compositionIdRequest }
                validateIdNotEmptyComposition("Проверка, что заголовок не пуст")
                validateIdProperFormatComposition("Проверка формата id", "composition")

                finishAdValidationComposition("Завершение проверок")
            }
            chain {
                title = "Логика чтения"
                repoReadComposition("Чтение состава из БД")
                repoSaveContext("Сохранение контекста в БД")
            }
            prepareResult("Подготовка ответа")
        }
    }.build();

}