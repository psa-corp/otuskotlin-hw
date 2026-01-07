package net.otuskotlin.ingredientscan.biz.common

import io.mockk.*
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.IsCorSettings
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsMessageSender
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class IsBizSubProcessorTest {

    private val sender: IsMessageSender = mockk(relaxed = true)
    private val settings = IsCorSettings(
        messageSender = sender,
        contextRepository = null
    )

    private val processor = IsBizSubProcessor(settings)

    @Test
    fun `COMPOSITION_CREATE triggers kafka send`() = runTest {
        val context = IsContext().apply {
            subCommand = IsSubCommand.COMPOSITION_CREATE
        }

        processor.exec(context)

        verify(exactly = 1) {
            sender.send(context)
        }
    }

    @Test
    fun `OCR_RECOGNITION triggers kafka send`() = runTest {
        val context = IsContext().apply {
            subCommand = IsSubCommand.OCR_RECOGNITION
        }

        processor.exec(context)

        verify(exactly = 1) {
            sender.send(context)
        }
    }

    @Test
    fun `errors prevent kafka sending`() = runTest {
        val context = IsContext().apply {
            subCommand = IsSubCommand.COMPOSITION_CREATE
            errors.add(mockk())
        }

        processor.exec(context)

        verify { sender wasNot Called }
    }
}
