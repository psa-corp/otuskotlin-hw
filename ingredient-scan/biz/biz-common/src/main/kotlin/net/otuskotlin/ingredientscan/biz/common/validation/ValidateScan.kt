package net.otuskotlin.ingredientscan.biz.common.validation

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorValidation
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsScanType
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.validateScanType(title: String, types: List<IsScanType>) = worker {
    this.title = title
    on { validateScan.type == IsScanType.NONE || !types.contains(validateScan.type) }
    handle {
        fail(
            errorValidation(
                field = "type",
                violationCode = "invalidType",
                description = "Scan type must be $types, got ${validateScan.type.name}"
            )
        )
    }
}

fun ICorChainDsl<IsContext>.validateTextNotEmptyScan(title: String) = worker {
    this.title = title
    on { validateScan.text.isEmpty() }
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

fun ICorChainDsl<IsContext>.validateFilesScan(title: String) = worker {
    this.title = title
    on { validateScan.files.isEmpty() }
    handle {
        fail(
            errorValidation(
                field = "files",
                violationCode = "empty",
                description = "field must not be empty"
            )
        )
    }
}

