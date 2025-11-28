package net.otuskotlin.ingredientscan.core.common.models

import kotlinx.datetime.Instant
import net.otuskotlin.ingredientscan.core.common.NONE

data class IsComponent(
    var id: IsComponentId = IsComponentId.NONE,
    var name: String = "",
    var createDate: Instant = Instant.NONE,
    var scientificName: String = "",
    var description: String = "",
    var sources: String = "",
    var riskLevel: IsRiskLevel = IsRiskLevel.NONE,
    var healthRisks: String = "",
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsComponent()
    }
}