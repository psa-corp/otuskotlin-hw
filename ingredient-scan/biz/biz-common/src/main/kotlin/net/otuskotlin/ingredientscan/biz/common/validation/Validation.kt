package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.chain

fun ICorChainDsl<IsContext>.validation(block: ICorChainDsl<IsContext>.() -> Unit) = chain {
    block()
    title = "Валидация"

    on { state == IsState.RUNNING }
}
