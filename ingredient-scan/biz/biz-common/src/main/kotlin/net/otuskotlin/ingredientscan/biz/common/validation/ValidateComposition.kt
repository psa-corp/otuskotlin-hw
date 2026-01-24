package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateTextNotEmptyComposition(title: String) = worker {
    this.title = title
    on { validateComposition.text.isEmpty() }
    handle {
        fail(
            errorValidation(
                field = "text",
                violationCode = "empty",
                description = "field must not be empty"
            )
        )
    }
}
