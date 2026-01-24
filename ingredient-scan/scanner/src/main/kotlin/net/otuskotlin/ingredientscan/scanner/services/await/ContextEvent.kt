package net.otuskotlin.ingredientscan.scanner.services.await
import net.otuskotlin.ingredientscan.core.common.external.IsContext


data class ContextEvent(val context: IsContext, val task : String)