package net.otuskotlin.ingredientscan.scanner.services.await

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.exceptions.TimeoutException
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorProcessing
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextAwaitService
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import net.otuskotlin.ingredientscan.scanner.services.await.Constants.Companion.TASK_READY
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
open class ContextAwaitService(
    private val appEventPublisher: ApplicationEventPublisher,
) : IsContextAwaitService {

    private val log = LoggerFactory.getLogger(ContextAwaitService::class.java)

    private val awaitingContexts = ConcurrentHashMap<IsContextId, ContextAwait>()
    private val mutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun await(context: IsContext, timeout: Long): IsLightContext {
        val deferred = CompletableDeferred<IsLightContext>()
        val contextAwait = ContextAwait(
            deferred = deferred,
            id = context.id,
            timeout = timeout
        )

        mutex.withLock {
            awaitingContexts[context.id] = contextAwait
        }

        val timeoutJob = coroutineScope.launch {
            try {
                delay(contextAwait.timeout)
                handleTimeout(context)
            } catch (e: CancellationException) {
                log.debug("Timeout job cancelled for context ${context.id}")
            }
        }

        return try {
            val completedContext = deferred.await()
            if (timeoutJob.isActive) {
                timeoutJob.cancel()
            }
            completedContext
        } catch (e: Exception) {
            when (e) {
                is TimeoutException -> {
                    context.fail(
                        errorProcessing(
                            field = "context",
                            violationCode = "timeout",
                            id = context.id.asString(),
                            timeout = e.timeoutMs
                        )
                    )
                }
                else -> {
                    context.fail(
                        errorProcessing(
                            field = "context",
                            violationCode = "exception",
                            id = context.id.asString(),
                            e = e,
                        )
                    )
                }
            }
            context.toLightContext()
        } finally {
            mutex.withLock {
                awaitingContexts.remove(context.id)
            }

            if (timeoutJob.isActive) {
                timeoutJob.cancel()
            }
        }
    }

    private suspend fun handleTimeout(context: IsContext) {
        mutex.withLock {
            val contextAwait = awaitingContexts.remove(context.id)
            contextAwait?.let {
                it.deferred.completeExceptionally(TimeoutException(it.timeout))
                log.warn("Context ${it.id} timeout after ${it.timeout}ms")
            }
        }
    }

    @EventListener
    suspend fun onContextReady(event: ContextEvent) {
        if (event.task != TASK_READY){
            return
        }

        log.info("Received context ready event for ${event.context.id}")

        val contextAwait = mutex.withLock {
            awaitingContexts.remove(event.context.id)
        }

        contextAwait?.let { track ->
            try {
                track.deferred.complete(event.context)
                log.info("Context ${event.context.id} delivered to waiting request")
            } catch (e: Exception) {
                log.error("Failed to complete deferred for ${event.context.id}", e)
            }
        }
    }
}