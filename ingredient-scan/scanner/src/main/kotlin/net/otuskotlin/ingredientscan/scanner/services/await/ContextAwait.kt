package net.otuskotlin.ingredientscan.scanner.services.await

import kotlinx.coroutines.CompletableDeferred
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import java.time.LocalDateTime

data class ContextAwait(
    val deferred: CompletableDeferred<IsLightContext>,
    val id: IsContextId,
    val created: LocalDateTime = LocalDateTime.now(),
    val timeout: Long = 300000L // Millis
){}