package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateIdProperFormatComposition(title: String, prefix: String) = worker {
    this.title = title

    // Может быть вынесен в IsCompositionId для реализации различных форматов
    val regExp = Regex("^${Regex.escape(prefix)}_[0-9a-zA-Z#:-]+$")
    on { validateCompositionId != IsCompositionId.NONE && !validateCompositionId.asString().matches(regExp) }
    handle {
        val encodedId = validateCompositionId.asString()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        fail(
            errorValidation(
                field = "id",
                violationCode = "badFormat",
                description = "value $encodedId must contain only letters and numbers"
            )
        )
    }
}
