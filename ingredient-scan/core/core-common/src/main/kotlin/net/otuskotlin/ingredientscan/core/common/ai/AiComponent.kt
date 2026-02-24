package net.otuskotlin.ingredientscan.core.common.ai

data class AiComponent(
    var name: String = "",
    var scientific_name: String = "",
    var description: String = "",
    var sources: String = "",
    var risk_level: String = "",
    var health_risks: String = "",
)