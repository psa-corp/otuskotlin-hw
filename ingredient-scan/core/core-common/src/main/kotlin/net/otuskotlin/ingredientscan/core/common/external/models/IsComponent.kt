package net.otuskotlin.ingredientscan.core.common.external.models

data class IsComponent(
    var name: String = "",
    var scientificName: String = "",
    var description: String = "",
    var sources: String = "",
    var riskLevel: IsRiskLevel = IsRiskLevel.NONE,
    var healthRisks: String = "",
) {
}