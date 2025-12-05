package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
import java.time.LocalDateTime

data class IsComponent(
    var id: IsComponentId = IsComponentId.NONE,
    var name: String = "",
    var createDate: LocalDateTime = LOCAL_DATE_TIME_NONE,
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