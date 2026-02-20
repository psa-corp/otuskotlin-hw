package net.otuskotlin.ingredientscan.scanner.services.biz


import net.otuskotlin.ingredientscan.app.internal.IsInternalAppSettings
import net.otuskotlin.ingredientscan.biz.common.IsBizInternalProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings

data class InternalAppSettings(
    override val settings: IsCorSettings,
    override val processor: IsBizInternalProcessor,
) : IsInternalAppSettings