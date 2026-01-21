package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.repoSaveContext(title: String) = worker {
    this.title = title
    description = "Сохранение контекста в БД"
    on { state == IsState.RUNNING }
    handle {
        try {
            contextRepo?.save(this)
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "context",
                    violationCode = "cannotSave",
                    description= description,
                    e = e,
                )
            )
        }
    }
}