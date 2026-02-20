package net.otuskotlin.ingredientscan.biz.common.sub

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.prepareToSubProcessor(title: String, sub: IsSubCommand) = worker {
    this.title = title
    description = "Подготовка данных для пост процесса"
    on { state == IsState.RUNNING }
    handle {
       subCommand = sub
    }
}

fun ICorChainDsl<IsContext>.checkAndPrepareToSubProcessor(title: String, sub: IsSubCommand) = worker {
    this.title = title
    description = "Если есть анализ,то возвращаем иначе создаем"
    on { state == IsState.RUNNING && analysis == IsAnalysis.NONE}
    handle {
        subCommand = sub
    }
}


fun ICorChainDsl<IsContext>.checkAndPrepareResult(title: String) = worker {
    this.title = title
    description = "Подготовка данных для ответа клиенту на запрос"
    on { subCommand == IsSubCommand.READY && state == IsState.RUNNING}
    handle {
        analysisResponse = analysis
        state = IsState.FINISHING
    }
}
