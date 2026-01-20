package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.app.common.IsAppSettings
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings

data class AppSettings(
    override val processor: IsBizProcessor,
    override val subProcessor: IsBizSubProcessor,
    override val settings: IsCorSettings
) : IsAppSettings {
}