package net.otuskotlin.ingredientscan.scanner

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3AsyncClient

@SpringBootTest(
    properties = [
        "spring.kafka.streams.auto-startup=false",
        "spring.kafka.bootstrap-servers="
    ]
)
@ActiveProfiles("test")
class ScannerApplicationTests {
    @MockitoBean
    lateinit var s3AsyncClient: S3AsyncClient

    @Test
    fun `context loads`() {
        // Smoke test - app starts without errors
    }
}