package net.otuskotlin.ingredientscan.app.common.models

import kotlinx.datetime.Instant

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