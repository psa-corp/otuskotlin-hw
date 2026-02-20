package net.otuskotlin.ingredientscan.biz.common.sub

import net.otuskotlin.ingredientscan.biz.common.exceptions.IsNotConfiguredException
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorSystem
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextAwaitService
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

fun ICorChainDsl<IsContext>.initContextAwaitService(title: String) = worker {
    this.title = title
    description = """
        Инит ожидателя ответа        
    """.trimIndent()
    handle {
        contextAwaitService = settings.contextAwaitService
        if (contextAwaitService == IsContextAwaitService.NONE) fail(
            errorSystem(
                violationCode = "serviceNotConfigured",
                e = IsNotConfiguredException("context await service")
            )
        )
    }
}

fun ICorChainDsl<IsContext>.initMessageSender(title: String) = worker {
    this.title = title
    description = """
        Инит отправителя сообщений в очередь
    """.trimIndent()
    handle {
        messageSender = settings.messageSender
        if (messageSender == IsMessageSender.NONE) fail(
            errorSystem(
                violationCode = "serviceNotConfigured",
                e = IsNotConfiguredException("context await service")
            )
        )
    }
}