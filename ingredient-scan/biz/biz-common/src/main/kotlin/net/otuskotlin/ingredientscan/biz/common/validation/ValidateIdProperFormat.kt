package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateIdProperFormatComposition(title: String, prefix: String) = worker {
    this.title = title

    // Может быть вынесен в IsCompositionId для реализации различных форматов
    val regExp = Regex("^${Regex.escape(prefix)}_[0-9a-zA-Z-]+$")
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


fun ICorChainDsl<IsContext>.validateIdProperFormatContext(title: String, prefix: String) = worker {
    this.title = title

    // Разрешаем два формата:
    // 1. prefix_custom-id (буквы, цифры, дефисы)
    // 2. prefix_uuid (где uuid - валидный UUID)
    val customIdRegExp = Regex("^${Regex.escape(prefix)}_[0-9a-zA-Z-]+\$")
    val uuidRegExp = Regex("^${Regex.escape(prefix)}_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$")

    on { validateContextId != IsContextId.NONE
            && !validateContextId.asString().matches(customIdRegExp)
            && !validateContextId.asString().matches(uuidRegExp)
    }
    handle {
        val encodedId = validateContextId.asString()
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        fail(
            errorValidation(
                field = "id",
                violationCode = "badFormat",
                description = "value $encodedId must be either: " +
                        "1. ${prefix}_[letters-numbers-dashes] " +
                        "2. ${prefix}_uuid (where uuid is in format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"
            )
        )
    }
}