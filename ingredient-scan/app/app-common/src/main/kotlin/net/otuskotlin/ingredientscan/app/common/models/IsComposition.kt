package net.otuskotlin.ingredientscan.app.common.models

import kotlinx.datetime.Instant

data class IsComposition(
    var id: IsCompositionId = IsCompositionId.NONE,
    var createDate: Instant = Instant.NONE,
    var text: String = "",
    var analysisId: IsAnalysisId = IsAnalysisId.NONE,
    var useCount: Long = 0,
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsComposition()
    }
}