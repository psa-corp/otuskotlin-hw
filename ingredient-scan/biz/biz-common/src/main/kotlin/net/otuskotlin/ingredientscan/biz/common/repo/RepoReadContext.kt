package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.mappers.toCompositionContext
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.repoReadCompositionContext(title: String) = worker {
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

fun ICorChainDsl<IsContext>.repoReadContext(title: String) = worker {
    this.title = title
    description = "Чтение состава из БД"
    on { state == IsState.RUNNING  }
    handle {
        try {
            val context = contextRepo?.findById(id)
                ?: throw RuntimeException("Context not found. ID:$id")

            compositionResponse = context.compositionResponse
            analysisResponse = context.analysisResponse
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