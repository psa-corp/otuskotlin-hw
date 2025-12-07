package net.otuskotlin.ingredientscan.core.common.external.models

import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
import java.time.LocalDateTime

data class IsScan(
    var id: IsScanId = IsScanId.NONE,
    var type: IsScanType = IsScanType.NONE,
    var time: LocalDateTime = LOCAL_DATE_TIME_NONE,
    var files: MutableList<String> = mutableListOf(),
    var text: String = "",
    var ocr: Long = 0,
    var compositionId: IsCompositionId = IsCompositionId.NONE,
    var userId: IsUserId = IsUserId.NONE,
    var category: String = "",
) {
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsScan()
    }
}