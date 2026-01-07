package net.otuskotlin.ingredientscan.scanner.services.biz

import io.mockk.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsRequestId
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate

class BizKafkaSenderTest {

    private val kafkaTemplate: KafkaTemplate<String, String> = mockk(relaxed = true)
    private val sender = BizKafkaSender(kafkaTemplate)

    @Test
    fun `COMPOSITION_CREATE sends message to composition-create topic`() {
        // arrange
        val context = IsContext().apply {
            id = IsContextId("context_1")
            subCommand = IsSubCommand.COMPOSITION_CREATE
        }

        // act
        sender.send(context)

        // assert
        verify(exactly = 1) {
            kafkaTemplate.send(
                BizKafkaSender.COMPOSITION_CREATE_TOPIC,
                "context_1",
                any()
            )
        }
    }

    @Test
    fun `OCR_RECOGNITION sends message to ocr-recognition topic`() {
        val context = IsContext().apply {
            id = IsContextId("context_2")
            subCommand = IsSubCommand.OCR_RECOGNITION
        }

        sender.send(context)

        verify {
            kafkaTemplate.send(
                BizKafkaSender.OCR_RECOGNITION_TOPIC,
                "context_2",
                any()
            )
        }
    }

    @Test
    fun `unsupported subCommand does not send anything`() {
        val context = IsContext().apply {
            id = IsContextId("context_3")
            subCommand = IsSubCommand.NONE
        }

        sender.send(context)

        verify { kafkaTemplate wasNot Called }
    }
}