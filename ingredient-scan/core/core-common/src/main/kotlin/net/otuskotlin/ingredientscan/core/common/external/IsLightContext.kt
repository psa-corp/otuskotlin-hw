package net.otuskotlin.ingredientscan.core.common.external

import net.otuskotlin.ingredientscan.core.common.external.models.*
import java.time.LocalDateTime

data class IsLightContext(
    var id: IsContextId = IsContextId.NONE,
    var command: IsCommand = IsCommand.NONE,
    var lightCommands: MutableList<IsLightCommand> = mutableListOf(),
    var subCommand: IsSubCommand = IsSubCommand.NONE,
    var state: IsState = IsState.NONE,
    val errors: MutableList<IsError> = mutableListOf(),

    var requestId: IsRequestId = IsRequestId.NONE,
    var timeStart: LocalDateTime = LOCAL_DATE_TIME_NONE,

    var scan: IsScan = IsScan.NONE,
    var analysis: IsAnalysis = IsAnalysis.NONE,
    var composition: IsComposition = IsComposition.NONE,

    var regenerateId: IsAnalysisId = IsAnalysisId.NONE,

){
    fun isEmpty() = this == NONE

    companion object {
        val NONE = IsLightContext()
    }
}