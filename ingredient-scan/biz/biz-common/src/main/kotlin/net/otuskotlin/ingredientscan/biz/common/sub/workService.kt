package net.otuskotlin.ingredientscan.biz.common.sub

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.sendContext(title: String) = worker {
    this.title = title
    description = "Отправляем запрос в очередь для обработки длительных операций"
    on { state == IsState.RUNNING }
    handle {
        try {
            messageSender?.send(this)
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "context",
                    violationCode = "cannotSend",
                    description= description,
                    e = e,
                )
            )
        }
    }
}

fun ICorChainDsl<IsContext>.awaitContext(title: String, timeout: Long) = worker {
    this.title = title
    description = "Ожидаем обработки контекста"
    on { state == IsState.RUNNING }
    handle {
        try {
         context = contextAwaitService?.await(this, timeout)
             ?: throw RuntimeException("Context must not be empty:$id")
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "context",
                    violationCode = "errorWhileWaiting",
                    description= description,
                    e = e,
                )
            )
        }
    }
}