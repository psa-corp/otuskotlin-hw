package net.otuskotlin.ingredientscan.core.common.ai


import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState

data class AIComposition(
    var text: String = "",
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = AIComposition()
    }
}