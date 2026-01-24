package net.otuskotlin.ingredientscan.biz.common.sub

import net.otuskotlin.ingredientscan.core.common.external.IsContext
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
