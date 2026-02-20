package net.otuskotlin.ingredientscan.app.common

import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.biz.common.IsBizProcessor
import net.otuskotlin.ingredientscan.biz.common.IsBizSubProcessor

interface IsAppSettings {
    val processor: IsBizProcessor
    val subProcessor: IsBizSubProcessor
    val settings: IsCorSettings
}
