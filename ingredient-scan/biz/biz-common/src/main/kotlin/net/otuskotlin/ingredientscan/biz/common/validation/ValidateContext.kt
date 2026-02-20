package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateIdContext(title: String) = worker {
    this.title = title
    on { context == IsLightContext.NONE || context.id != id }
    handle {
        fail(
            errorValidation(
                field = "id",
                violationCode = "incorrectly",
                description = "ids must be the same"
            )
        )
    }
}
