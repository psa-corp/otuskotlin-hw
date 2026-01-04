//package net.otuskotlin.ingredientscan.scanner.services.kafka
//
//import net.otuskotlin.ingredientscan.core.common.external.IsContext
//import net.otuskotlin.ingredientscan.core.common.external.models.*
//import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
//import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
//import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Nested
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.test.context.TestPropertySource
//import org.springframework.test.context.bean.override.mockito.MockitoBean
//import java.util.UUID
//
//@SpringBootTest
//@TestPropertySource(properties = [
//    // Отключаем Kafka
//    "spring.kafka.bootstrap-servers=",
//    "spring.kafka.streams.auto-startup=false",
//
//    // Отключаем S3
//    "spring.cloud.aws.enabled=false",
//    "spring.cloud.aws.region.static=us-east-1",
//    "spring.cloud.aws.credentials.access-key=test-key",
//    "spring.cloud.aws.credentials.secret-key=test-secret",
//    "spring.cloud.aws.s3.endpoint=http://localhost:9090",
//    "spring.cloud.aws.s3.path-style-access-enabled=true",
//
//    // Отключаем Web контекст
//    "spring.main.web-application-type=none"
//])
//class BizServiceKafkaTest {
//
//    @Autowired
//    private lateinit var bizService: BizService
//
//    @MockitoBean
//    private lateinit var kafkaTemplate: KafkaTemplate<String, String>
//
//    @Autowired
//    private lateinit var compositionRepository: InMemoryCompositionRepository
//
//    @Autowired
//    private lateinit var contextRepository: InMemoryContextRepository
//
//    @BeforeEach
//    fun setUp() {
//        compositionRepository.clear()
//        contextRepository.clear()
//    }
//
//    @Nested
//    inner class CompositionCreateByManualTests {
//
//        @Test
//        fun `composition create by manual sends context to Kafka and returns running state`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-001")
//                compositionRequest = IsComposition(text = "Water, Salt, Sugar")
//            }
//
//            // Act
//            val result = bizService.compositionCreateByManual(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, result.state)
//            assertEquals(IsCommand.COMPOSITION_CREATE_MANUAL, result.command)
//            assertTrue(result.errors.isEmpty())
//            assertNotNull(result.requestId)
//        }
//
//        @Test
//        fun `composition create by manual returns failing state and error when text is empty`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-002")
//                compositionRequest = IsComposition(text = "")
//            }
//
//            // Act
//            val result = bizService.compositionCreateByManual(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, result.state)
//            assertTrue(result.errors.isNotEmpty())
//            assertEquals("COMPOSITION_TEXT_EMPTY", result.errors[0].code)
//        }
//
//        @Test
//        fun `composition create by manual returns failing state and error when text contains only whitespace`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-003")
//                compositionRequest = IsComposition(text = "   \n\t  ")
//            }
//
//            // Act
//            val result = bizService.compositionCreateByManual(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, result.state)
//            assertTrue(result.errors.isNotEmpty())
//        }
//
//        @Test
//        fun `composition create by manual successfully processes UTF8 text`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-004")
//                compositionRequest = IsComposition(text = "Вода, Соль, Сахар")
//            }
//
//            // Act
//            val result = bizService.compositionCreateByManual(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, result.state)
//            assertTrue(result.errors.isEmpty())
//            assertEquals("Вода, Соль, Сахар", result.compositionRequest.text)
//        }
//
//        @Test
//        fun `composition create by manual successfully processes text with special characters`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("context-005")
//                compositionRequest = IsComposition(text = "H2O & Salt; (Sugar) - 50%")
//            }
//
//            // Act
//            val result = bizService.compositionCreateByManual(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, result.state)
//            assertTrue(result.errors.isEmpty())
//        }
//    }
//
//    @Nested
//    inner class CompositionCreateByPhotosTests {
//
//        @Test
//        fun `composition create by photos sends context to Kafka and returns running state`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-006")
//                scanRequest = IsScan(
//                    files = mutableListOf(
//                        UUID.randomUUID().toString() + "_photo1.jpg",
//                        UUID.randomUUID().toString() + "_photo2.jpg"
//                    )
//                )
//            }
//
//            // Act
//            val result = bizService.compositionCreateByPhotos(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, result.state)
//            assertEquals(IsCommand.COMPOSITION_CREATE_PHOTOS, result.command)
//            assertTrue(result.errors.isEmpty())
//        }
//
//        @Test
//        fun `composition create by photos returns failing state and error when files list is empty`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-007")
//                scanRequest = IsScan(files = mutableListOf())
//            }
//
//            // Act
//            val result = bizService.compositionCreateByPhotos(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, result.state)
//            assertTrue(result.errors.isNotEmpty())
//            assertEquals("PHOTOS_EMPTY", result.errors[0].code)
//        }
//    }
//
//    @Nested
//    inner class CompositionGetTests {
//
//        @Test
//        fun `composition get successfully retrieves composition from repository`() {
//            // Arrange
//            val compositionId = IsCompositionId("comp-001")
//            val composition = IsComposition(
//                id = compositionId,
//                text = "Вода, Соль, Сахар"
//            )
//            compositionRepository.save(composition)
//
//            val context = IsContext().apply {
//                compositionIdRequest = compositionId
//            }
//
//            // Act
//            val result = bizService.compositionGet(context)
//
//            // Assert
//            assertEquals(IsState.FINISHING, result.state)
//            assertEquals(composition.id, result.compositionResponse.id)
//            assertTrue(result.errors.isEmpty())
//        }
//
//        @Test
//        fun `composition get returns failing state and error when composition not found`() {
//            // Arrange
//            val context = IsContext().apply {
//                compositionIdRequest = IsCompositionId("non-existent")
//            }
//
//            // Act
//            val result = bizService.compositionGet(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, result.state)
//            assertTrue(result.errors.isNotEmpty())
//            assertEquals("COMPOSITION_NOT_FOUND", result.errors[0].code)
//        }
//
//        @Test
//        fun `composition get successfully retrieves multiple compositions from repository`() {
//            // Arrange
//            val comp1 = IsComposition(
//                id = IsCompositionId("comp-1"),
//                text = "Water"
//            )
//            val comp2 = IsComposition(
//                id = IsCompositionId("comp-2"),
//                text = "Salt"
//            )
//            compositionRepository.save(comp1)
//            compositionRepository.save(comp2)
//
//            // Act
//            val result1 = bizService.compositionGet(IsContext().apply {
//                compositionIdRequest = IsCompositionId("comp-1")
//            })
//
//            val result2 = bizService.compositionGet(IsContext().apply {
//                compositionIdRequest = IsCompositionId("comp-2")
//            })
//
//            // Assert
//            assertEquals(comp1.id, result1.compositionResponse.id)
//            assertEquals(comp2.id, result2.compositionResponse.id)
//        }
//    }
//
//    @Nested
//    inner class CompositionContextGetTests {
//
//        @Test
//        fun `composition context get returns failing state and error when context not found`() {
//            // Arrange
//            val context = IsContext().apply {
//                contextIdRequest = IsContextId("non-existent")
//            }
//
//            // Act
//            val result = bizService.compositionContextGet(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, result.state)
//            assertTrue(result.errors.isNotEmpty())
//            assertEquals("CONTEXT_NOT_FOUND", result.errors[0].code)
//        }
//    }
//}