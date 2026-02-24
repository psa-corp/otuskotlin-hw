package net.otuskotlin.ingredientscan.core.common.ai


import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsError

data class AiAnalysis(
    var description: String = "",
    var rating: Double = -1.0,
    var color: IsColor = IsColor.NONE,
    val components: MutableList<AiComponent> = mutableListOf(),
    val errors: MutableList<IsError> = mutableListOf(),
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = AiAnalysis()
    }
}