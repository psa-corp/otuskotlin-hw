package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<InternalContext>.repoSaveComposition(title: String) = worker {
    this.title = title
    description = "Сохранение контекста в БД"
    on { state != IsState.NONE }
    handle {
        try {
            compositionRepo?.save(compositionRequest)
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "composition",
                    violationCode = "cannotSave",
                    description= description,
                    e = e,
                )
            )
        }
    }
}