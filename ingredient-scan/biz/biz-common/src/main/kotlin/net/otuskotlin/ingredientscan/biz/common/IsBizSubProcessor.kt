package net.otuskotlin.ingredientscan.biz.common

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand

class IsBizSubProcessor(private val settings: IsCorSettings) {
    suspend fun exec(context: IsContext) {

        if (!context.errors.isEmpty()) {
            return
        }

        when (context.subCommand){
            IsSubCommand.COMPOSITION_CREATE, IsSubCommand.OCR_RECOGNITION -> {
                settings.messageSender?.let { it.send(context) }
            }
            else -> {}
        }
        settings.contextRepository?.save(context)
    }
}