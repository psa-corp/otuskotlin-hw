package net.otuskotlin.ingredientscan.core.common.models

import kotlinx.datetime.Instant
import net.otuskotlin.ingredientscan.core.common.NONE

data class IsScan(
    var id: IsScanId = IsScanId.NONE,
    var type: IsScanType = IsScanType.NONE,
    var time: Instant = Instant.NONE,
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