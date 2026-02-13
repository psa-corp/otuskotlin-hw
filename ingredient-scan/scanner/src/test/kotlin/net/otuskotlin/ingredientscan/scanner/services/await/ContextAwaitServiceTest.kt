package net.otuskotlin.ingredientscan.scanner.services.await

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.mappers.toLightContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class ContextAwaitServiceTest {

    private lateinit var service: ContextAwaitService
    private lateinit var publisher: ApplicationEventPublisher

    @BeforeEach
    fun setUp() {
        publisher = mock()
        service = ContextAwaitService(publisher)
    }

    @Test
    fun `await should complete successfully when event arrives`() = runTest {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val context = IsContext(id = contextId)
        val lightContextResult = IsLightContext(id = contextId) // упрощенно

        val deferred = async {
            service.await(context, timeout = 5000)
        }

        yield()

        val event = ContextEvent(lightContextResult, Constants.TASK_READY)
        service.onContextReady(event)

        val result = deferred.await()
        assertEquals(lightContextResult, result)
        assertTrue(context.errors.isEmpty())
    }

    @Test
    fun `await should timeout when no event arrives`() = runTest {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val context = IsContext(id = contextId)
        val timeoutMs = 1000L

        val deferred = async {
            service.await(context, timeout = timeoutMs)
        }

        testScheduler.advanceTimeBy(timeoutMs + 100)

        val result = deferred.await()
        assertEquals(context.toLightContext(), result)
        assertEquals(1, context.errors.size)
        assertEquals("processing-context-timeout", context.errors.first().code)
    }

    @Test
    fun `await should handle multiple concurrent requests`() = runTest {
        val contextId1 = IsContextId("1")
        val contextId2 = IsContextId("2")
        val context1 = IsContext(id = contextId1)
        val context2 = IsContext(id = contextId2)
        val light1 = IsLightContext(id = contextId1)
        val light2 = IsLightContext(id = contextId2)

        val deferred1 = async { service.await(context1, timeout = 5000) }
        val deferred2 = async { service.await(context2, timeout = 5000) }

        yield()

        service.onContextReady(ContextEvent(light1, Constants.TASK_READY))

        val result1 = deferred1.await()
        assertEquals(light1, result1)

        assertFalse(deferred2.isCompleted)

        service.onContextReady(ContextEvent(light2, Constants.TASK_READY))

        val result2 = deferred2.await()
        assertEquals(light2, result2)
    }

    @Test
    fun `await should ignore events with wrong task`() = runTest {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val context = IsContext(id = contextId)
        val light = IsLightContext(id = contextId)

        val deferred = async {
            service.await(context, timeout = 5000)
        }

        yield()

        service.onContextReady(ContextEvent(light, "WRONG_TASK"))
        assertFalse(deferred.isCompleted)
        deferred.cancel()
    }

    @Test
    fun `await should handle cancellation before event`() = runTest {
        val contextId = IsContextId(UUID.randomUUID().toString())
        val context = IsContext(id = contextId)

        val deferred = async {
            service.await(context, timeout = 5000)
        }

        yield()

        deferred.cancel()
        assertTrue(deferred.isCancelled)
        service.onContextReady(ContextEvent(IsLightContext(id = contextId), Constants.TASK_READY))
    }

    private fun mock(): ApplicationEventPublisher = ApplicationEventPublisher {}
}