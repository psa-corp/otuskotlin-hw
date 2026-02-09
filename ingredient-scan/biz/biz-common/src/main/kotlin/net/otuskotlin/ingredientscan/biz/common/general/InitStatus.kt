package net.otuskotlin.ingredientscan.biz.common.general

import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState

@JvmName("initStatus")
fun ICorChainDsl<IsContext>.initStatus(title: String) = worker() {
    this.title = title
    this.description = """
        Этот обработчик устанавливает стартовый статус обработки. Запускается только в случае не заданного статуса.
    """.trimIndent()
    on { state == IsState.NONE }
    handle { state = IsState.RUNNING }
}

@JvmName("initStatusInternal")
fun ICorChainDsl<InternalContext>.initStatus(title: String) = worker() {
    this.title = title
    this.description = """
        Этот обработчик устанавливает стартовый статус обработки. Запускается только в случае не заданного статуса.
    """.trimIndent()
    on { state == IsState.NONE }
    handle { state = IsState.RUNNING }
}