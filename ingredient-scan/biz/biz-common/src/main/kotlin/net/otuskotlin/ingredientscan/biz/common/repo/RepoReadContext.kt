package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker
import net.otuskotlin.ingredientscan.mappers.v1.toCompositionContext

fun ICorChainDsl<IsContext>.repoReadContext(title: String) = worker {
    this.title = title
    description = "Чтение состава из БД"
    on { state == IsState.RUNNING}
    handle {
        try {
             val context = contextRepo?.findById(validatedContextId)
                ?: throw RuntimeException("Context not found. ID:$validatedContextId")

             compositionContextResponse = context.toCompositionContext()
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "composition",
                    violationCode = "badRead",
                    description= description,
                    e = e,
                )
            )
        }
    }
}