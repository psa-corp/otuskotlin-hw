//package net.otuskotlin.ingredientscan.scanner.services.kafka
//
//import net.otuskotlin.ingredientscan.core.common.external.IsContext
//import net.otuskotlin.ingredientscan.core.common.external.models.*
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Nested
//import org.junit.jupiter.api.Test
//
//class ProcessorsKafkaTest {
//
//    @Nested
//    inner class CompositionValidateProcessorTests {
//
//        @Test
//        fun `validation success`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-001")
//                compositionRequest = IsComposition(text = "Water, Salt, Sugar")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, resultContext.state)
//            assertTrue(resultContext.errors.isEmpty())
//            assertEquals("Water, Salt, Sugar", resultContext.compositionRequest.text)
//        }
//
//        @Test
//        fun `validation empty text`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-002")
//                compositionRequest = IsComposition(text = "")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, resultContext.state)
//            assertTrue(resultContext.errors.isNotEmpty())
//            assertEquals("TEXT_EMPTY", resultContext.errors[0].code)
//        }
//
//        @Test
//        fun `validation skip if errors`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-004")
//                compositionRequest = IsComposition(text = "Valid")
//                state = IsState.FAILING
//                errors.add(IsError(
//                    code = "EXISTING_ERROR",
//                    group = "TEST",
//                    field = "test",
//                    message = "Already has error"
//                ))
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, resultContext.state)
//            assertEquals(1, resultContext.errors.size)
//            assertEquals("EXISTING_ERROR", resultContext.errors[0].code)
//        }
//
//        @Test
//        fun `validation special characters`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-007")
//                compositionRequest = IsComposition(text = "H2O & Salt; (Sugar) - 50%")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, resultContext.state)
//            assertTrue(resultContext.errors.isEmpty())
//        }
//
//        @Test
//        fun `validation very long text`() {
//            // Arrange
//            val longText = "Ingredient ".repeat(1000)
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-008")
//                compositionRequest = IsComposition(text = longText)
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.RUNNING, resultContext.state)
//            assertTrue(resultContext.errors.isEmpty())
//        }
//
//        @Test
//        fun `validation whitespace only text`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-val-009")
//                compositionRequest = IsComposition(text = "   \n\t  ")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processValidate(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, resultContext.state)
//            assertTrue(resultContext.errors.isNotEmpty())
//            assertEquals("TEXT_EMPTY", resultContext.errors[0].code)
//        }
//
//        private fun processValidate(context: IsContext): IsContext {
//            if (context.state == IsState.FAILING) {
//                return context
//            }
//
//            val text = context.compositionRequest.text.trim()
//
//            if (text.isBlank()) {
//                context.state = IsState.FAILING
//                context.errors.add(IsError(
//                    code = "TEXT_EMPTY",
//                    group = "VALIDATION",
//                    field = "text",
//                    message = "Text cannot be empty or contain only whitespace"
//                ))
//                return context
//            }
//
//            context.state = IsState.RUNNING
//            return context
//        }
//    }
//
//    @Nested
//    inner class CompositionSaveProcessorTests {
//
//        @Test
//        fun `save success`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-save-001")
//                compositionRequest = IsComposition(text = "Water, Salt")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val resultContext = processSave(context)
//
//            // Assert
//            assertEquals(IsState.FINISHING, resultContext.state)
//            assertTrue(resultContext.errors.isEmpty())
//            assertNotEquals(IsCompositionId.NONE, resultContext.compositionResponse.id)
//        }
//
//        @Test
//        fun `save idempotency`() {
//            // Arrange
//            val context1 = IsContext().apply {
//                requestId = IsRequestId("req-save-002a")
//                compositionRequest = IsComposition(text = "Same composition")
//                state = IsState.RUNNING
//            }
//            val context2 = IsContext().apply {
//                requestId = IsRequestId("req-save-002b")
//                compositionRequest = IsComposition(text = "Same composition")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val context1Result = processSave(context1)
//            val context2Result = processSave(context2)
//
//            // Assert
//            assertNotNull(context1Result.compositionResponse.id)
//            assertNotNull(context2Result.compositionResponse.id)
//
//            assertEquals(context1Result.compositionResponse.id, context2Result.compositionResponse.id)
//        }
//
//        @Test
//        fun `save skip if errors`() {
//            // Arrange
//            val context = IsContext().apply {
//                requestId = IsRequestId("req-save-003")
//                compositionRequest = IsComposition(text = "Valid")
//                state = IsState.FAILING
//                errors.add(IsError(
//                    code = "VALIDATION_ERROR",
//                    group = "VALIDATION",
//                    field = "text",
//                    message = "Invalid text"
//                ))
//            }
//
//            // Act
//            val resultContext = processSave(context)
//
//            // Assert
//            assertEquals(IsState.FAILING, resultContext.state)
//            assertEquals(IsCompositionId.NONE, resultContext.compositionResponse.id)
//            assertTrue(resultContext.errors.isNotEmpty())
//        }
//
//        @Test
//        fun `save different texts get different ids`() {
//            // Arrange
//            val context1 = IsContext().apply {
//                requestId = IsRequestId("req-save-004a")
//                compositionRequest = IsComposition(text = "Water")
//                state = IsState.RUNNING
//            }
//            val context2 = IsContext().apply {
//                requestId = IsRequestId("req-save-004b")
//                compositionRequest = IsComposition(text = "Salt")
//                state = IsState.RUNNING
//            }
//
//            // Act
//            val context1Result = processSave(context1)
//            val context2Result = processSave(context2)
//
//            // Assert
//            assertNotEquals(context1Result.compositionResponse.id, context2Result.compositionResponse.id)
//        }
//
//        private fun processSave(context: IsContext): IsContext {
//            if (context.state == IsState.FAILING || context.errors.isNotEmpty()) {
//                return context
//            }
//
//            val textHash = context.compositionRequest.text.hashCode().toString().replace("-", "x")
//            val compositionId = IsCompositionId("comp-$textHash")
//
//            context.compositionResponse = IsComposition(
//                id = compositionId,
//                text = context.compositionRequest.text.split(",").map { it.trim() }.toString()
//            )
//
//            context.state = IsState.FINISHING
//            return context
//        }
//    }
//}