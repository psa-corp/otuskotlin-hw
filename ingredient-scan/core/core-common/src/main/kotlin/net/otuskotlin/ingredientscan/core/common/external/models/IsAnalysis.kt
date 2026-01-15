package net.otuskotlin.ingredientscan.core.common.external.models


import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
import java.time.LocalDateTime

data class IsAnalysis(
    var id: IsAnalysisId = IsAnalysisId.NONE,
    var compositionId: IsCompositionId = IsCompositionId.NONE,
    var createDate: LocalDateTime = LOCAL_DATE_TIME_NONE,
    var description: String = "",
    var rating: Double = 0.0,
    var color: IsColor = IsColor.NONE,
    var problematicComponents: MutableList<IsComponent> = mutableListOf(),
    var safeComponents: MutableList<IsComponent> = mutableListOf(),
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsAnalysis()
    }
}