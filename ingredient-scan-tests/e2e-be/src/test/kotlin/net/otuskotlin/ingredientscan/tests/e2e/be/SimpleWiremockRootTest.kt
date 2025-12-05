package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseWiremockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleWiremockRootTest : BaseWiremockTest() {

    @Test
    fun `WireMock should return Hello World on root request`() {
        val response = executeGet("/")

        assertEquals(200, response.code)
        assertEquals("Hello, world!", response.body?.string())
    }
}