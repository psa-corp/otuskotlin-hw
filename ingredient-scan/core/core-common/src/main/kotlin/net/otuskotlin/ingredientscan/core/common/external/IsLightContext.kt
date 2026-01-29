package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.*
import java.time.LocalDateTime

data class IsLightContext(
    var id: IsContextId = IsContextId.NONE,
    var command: IsCommand = IsCommand.NONE,
    var subCommand: IsSubCommand = IsSubCommand.NONE,
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),

    var requestId: IsRequestId = IsRequestId.NONE,
    var timeStart: LocalDateTime = LOCAL_DATE_TIME_NONE,
){
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsLightContext()
    }
}