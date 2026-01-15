package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
import java.time.LocalDateTime

data class IsCompositionContext(
    var id: IsContextId = IsContextId.NONE,
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),
    var timeStart: LocalDateTime = LOCAL_DATE_TIME_NONE,
    var composition: IsComposition = IsComposition()
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsCompositionContext()
    }
}