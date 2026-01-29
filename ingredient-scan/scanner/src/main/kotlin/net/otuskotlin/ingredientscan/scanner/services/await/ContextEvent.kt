package net.otuskotlin.ingredientscan.scanner.services.await
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext


data class ContextEvent(val context: IsLightContext, val task : String)