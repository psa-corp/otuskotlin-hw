package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorRepo
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.repoReadAnalysis(title: String) = worker {
    this.title = title
    description = "Чтение состава из БД"
    on { state == IsState.RUNNING}
    handle {
        try {
            analysis = analysisRepo?.findAnalysisById(validatedAnalysisId)
                ?: throw RuntimeException("Composition not found. ID:$validatedAnalysisId")
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "analysis",
                    violationCode = "badRead",
                    description= description,
                    e = e,
                )
            )
        }
    }
}

fun ICorChainDsl<IsContext>.repoReadAnalysisByComposition(title: String) = worker {
    this.title = title
    description = "Чтение состава из БД"
    on { state == IsState.RUNNING}
    handle {
        try {
            analysis = analysisRepo?.findAnalysisByCompositionId(validatedCompositionId) ?: IsAnalysis.NONE
        } catch (e: Throwable) {
            fail(
                errorRepo(
                    field = "analysis",
                    violationCode = "badRead",
                    description= description,
                    e = e,
                )
            )
        }
    }
}