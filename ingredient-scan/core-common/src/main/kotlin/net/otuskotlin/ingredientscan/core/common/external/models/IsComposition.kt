package net.otuskotlin.ingredientscan.core.common.external.models


import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
import java.time.LocalDateTime

data class IsComposition(
    var id: IsCompositionId = IsCompositionId.NONE,
    var createDate: LocalDateTime = LOCAL_DATE_TIME_NONE,
    var text: String = "",
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsComposition()
    }
}