package net.otuskotlin.ingredientscan.scanner

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ScannerApplicationTests {

    @Test
    fun `context loads`() {
        // Smoke test - app starts without errors
    }
}