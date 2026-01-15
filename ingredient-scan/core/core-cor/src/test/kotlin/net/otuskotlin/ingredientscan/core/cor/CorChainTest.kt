package net.otuskotlin.ingredientscan.core.cor


import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.cor.handlers.CorChain
import net.otuskotlin.ingredientscan.core.cor.handlers.CorWorker

import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CorChainTest {

    @Test
    fun `chain should execute workers`() = runTest {
        val createWorker = { title: String ->
            CorWorker<TestContext>(
                title = title,
                blockOn = { status == TestContext.CorStatuses.NONE },
                blockHandle = { history += "$title; " }
            )
        }
        val chain = CorChain<TestContext>(
            execs = listOf(createWorker("w1"), createWorker("w2")),
            title = "chain",
        )
        val ctx = TestContext()
        chain.exec(ctx)
        assertEquals("w1; w2; ", ctx.history)
    }
}
