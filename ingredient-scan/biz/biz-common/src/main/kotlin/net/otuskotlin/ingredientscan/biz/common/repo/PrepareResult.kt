package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.prepareResult(title: String) = worker {
    this.title = title
    description = "Подготовка данных для ответа клиенту на запрос"
    on { state == IsState.RUNNING || state == IsState.FINISHING }
    handle {
        state = when (val st = state) {
            IsState.RUNNING -> IsState.FINISHING
            else -> st
        }
    }
}

fun ICorChainDsl<InternalContext>.prepareResult(title: String) = worker {
    this.title = title
    description = "Подготовка данных для ответа клиенту на запрос"
    on { state == IsState.RUNNING || state == IsState.FINISHING }
    handle {
        state = when (val st = state) {
            IsState.RUNNING -> IsState.FINISHING
            else -> st
        }
    }
}