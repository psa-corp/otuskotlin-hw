package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateIdProperFormatComposition(title: String, prefix: String) = worker {
    this.title = title

    val idPattern = Regex("^${Regex.escape(prefix)}-[0-9a-zA-Z-]{1,100}$")
    on { validateCompositionId != IsCompositionId.NONE
            && !validateCompositionId.asString().matches(idPattern)
    }
    handle {
        val encodedId = validateCompositionId.asString()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        val idStr = validateCompositionId.asString()
        fail(
            errorValidation(
                field = "id",
                violationCode = "badFormat",
                description = "ID '$idStr' must start with '$prefix-' and contain only hex/letters/numbers/dashes"
            )
        )
    }
}


fun ICorChainDsl<IsContext>.validateIdProperFormatContext(title: String, prefix: String) = worker {
    this.title = title

    val idPattern = Regex("^${Regex.escape(prefix)}-[0-9a-zA-Z-]{1,100}$")

    on { validateContextId != IsContextId.NONE
            && !validateContextId.asString().matches(idPattern)
    }
    handle {
        val encodedId = validateContextId.asString()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        val idStr = validateCompositionId.asString()
        fail(
            errorValidation(
                field = "id",
                violationCode = "badFormat",
                description = "ID '$idStr' must start with '$prefix-' and contain only hex/letters/numbers/dashes"
            )
        )
    }
}
//on { validateAnalysisId != IsAnalysisId.NONE && !validateAnalysisId.asString().matches(regExp) }

fun ICorChainDsl<IsContext>.validateIdProperFormatAnalysis(title: String, prefix: String) = worker {
    this.title = title

    val idPattern = Regex("^${Regex.escape(prefix)}-[0-9a-zA-Z-]{1,100}$")

    on { validateAnalysisId != IsAnalysisId.NONE
            && !validateAnalysisId.asString().matches(idPattern)
    }
    handle {
        val encodedId = validateAnalysisId.asString()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        val idStr = validateCompositionId.asString()
        fail(
            errorValidation(
                field = "id",
                violationCode = "badFormat",
                description = "ID '$idStr' must start with '$prefix-' and contain only hex/letters/numbers/dashes"
            )
        )
    }
}