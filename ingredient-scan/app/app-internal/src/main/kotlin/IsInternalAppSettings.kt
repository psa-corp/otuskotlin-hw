package net.otuskotlin.ingredientscan.app.internal

import net.otuskotlin.ingredientscan.biz.common.IsBizInternalProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings

interface IsInternalAppSettings {
    val processor: IsBizInternalProcessor
    val settings: IsCorSettings
}
