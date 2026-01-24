package net.otuskotlin.ingredientscan.scanner.services.await

import kotlinx.coroutines.CompletableDeferred
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import java.time.LocalDateTime

data class ContextAwait(
    val deferred: CompletableDeferred<IsContext>,
    val id: IsContextId,
    val created: LocalDateTime = LocalDateTime.now(),
    val timeout: Long = 300000L // Millis
){}