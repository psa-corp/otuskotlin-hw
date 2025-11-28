package net.otuskotlin.ingredientscan.app.common.models


import kotlinx.datetime.Instant

data class IsAnalysis(
    var id: IsAnalysisId = IsAnalysisId.NONE,
    var compositionId: IsCompositionId = IsCompositionId.NONE,
    var createDate: Instant = Instant.NONE,
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