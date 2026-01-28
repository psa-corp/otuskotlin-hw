package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker


fun ICorChainDsl<IsContext>.finishValidationCompositionId(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedCompositionId = validateCompositionId
    }
}

fun ICorChainDsl<IsContext>.finishValidationContext(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedContextId = validateContextId
    }
}

fun ICorChainDsl<IsContext>.finishValidationAnalysisId(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedAnalysisId = validateAnalysisId
    }
}

fun ICorChainDsl<IsContext>.finishValidationAnalysis(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedAnalysis = validateAnalysis
    }
}

fun ICorChainDsl<IsContext>.finishValidationScan(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedScan = validateScan
    }
}

fun ICorChainDsl<IsContext>.finishValidationComposition(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedComposition = validateComposition
    }
}

//fun ICorChainDsl<IsContext>.finishAdFilterValidation(title: String) = worker {
//    this.title = title
//    on { state == IsState.RUNNING }
//    handle {
//        adFilterValidated = adFilterValidating
//    }
//}
