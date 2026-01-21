package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker


fun ICorChainDsl<IsContext>.finishAdValidationComposition(title: String) = worker {
    this.title = title
    on { state == IsState.RUNNING }
    handle {
        validatedCompositionId = validateCompositionId
    }
}

//fun ICorChainDsl<IsContext>.finishAdFilterValidation(title: String) = worker {
//    this.title = title
//    on { state == IsState.RUNNING }
//    handle {
//        adFilterValidated = adFilterValidating
//    }
//}
