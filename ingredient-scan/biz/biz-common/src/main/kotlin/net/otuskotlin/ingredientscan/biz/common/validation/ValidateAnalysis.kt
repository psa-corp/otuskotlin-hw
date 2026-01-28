package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateDescriptionNotEmptyAnalysis(title: String) = worker {
    this.title = title
    on { validateAnalysis.description.isEmpty() }
    handle {
        fail(
            errorValidation(
                field = "description",
                violationCode = "empty",
                description = "field must not be empty"
            )
        )
    }
}

fun ICorChainDsl<IsContext>.validateColorNotEmptyAnalysis(title: String) = worker {
    this.title = title
    on { validateAnalysis.color == IsColor.NONE }
    handle {
        fail(
            errorValidation(
                field = "color",
                violationCode = "empty",
                description = "field must not be empty"
            )
        )
    }
}

fun ICorChainDsl<IsContext>.validateHasRatingAnalysis(title: String) = worker {
    this.title = title
    on { validateAnalysis.rating >= 0.0 }
    handle {
        fail(
            errorValidation(
                field = "rating",
                violationCode = "empty",
                description = "field must not be empty"
            )
        )
    }
}