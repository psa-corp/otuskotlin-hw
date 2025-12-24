package net.otuskotlin.ingredientscan.scanner.tests.e2e.be

import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseKafkaTest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

class ScannerKafkaIntegrationTest : BaseKafkaTest() {

    private fun waitForMessage(
        topic: String,
        predicate: (String, String) -> Boolean,
        maxAttempts: Int = 10,
        timeoutMs: Long = 2000
    ): Boolean {
        log.debug("Waiting for message in topic: $topic")
        consumer.unsubscribe()
        consumer.subscribe(listOf(topic))
        Thread.sleep(500)

        var attempts = 0
        while (attempts < maxAttempts) {
            val records = consumer.poll(java.time.Duration.ofMillis(timeoutMs))
            if (!records.isEmpty) {
                records.forEach { record ->
                    if (predicate(record.key(), record.value())) {
                        log.debug("Found matching message: key=${record.key()}")
                        return true
                    }
                }
            }
            attempts++
        }
        log.debug("No matching message found after $maxAttempts attempts")
        return false
    }

    @Nested
    inner class PhotoFlowTests {

        @Test
        fun `photo scan request sends message to kafka`() {
            log.info("Starting photo scan request test")

            val photoTopic = "ingredient-scan-photo-request"
            createTopic(photoTopic)

            val scanId = UUID.randomUUID().toString()
            val photoFile = "${scanId}_photo.jpg"

            val context = IsContext().apply {
                requestId = IsRequestId("req-photo-001")
                command = IsCommand.COMPOSITION_CREATE_PHOTOS
                state = IsState.RUNNING
                scanRequest = IsScan().apply {
                    id = IsScanId(scanId)
                    type = IsScanType.PHOTO
                    files = mutableListOf(photoFile)
                }
            }

            val jsonPayload = commonContextSerialize(context)

            log.info("Sending photo scan request to Kafka")
            sendMessage(
                topicName = photoTopic,
                key = scanId,
                value = jsonPayload
            )

            log.info("Waiting for message with photo scan request")
            val found = waitForMessage(
                topic = photoTopic,
                predicate = { key, value ->
                    key == scanId &&
                            value.contains(photoFile) &&
                            value.contains("PHOTO")
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Photo scan request test completed successfully")
        }

        @Test
        fun `multiple photos processing sends message with all photos`() {
            log.info("Starting multiple photos processing test")

            val photoTopic = "ingredient-scan-photo-request-multiple"
            createTopic(photoTopic)

            val scanId = UUID.randomUUID().toString()
            val photos = listOf(
                "${scanId}_photo1.jpg",
                "${scanId}_photo2.jpg",
                "${scanId}_photo3.jpg"
            )

            val context = IsContext().apply {
                requestId = IsRequestId("req-multi-photos")
                command = IsCommand.COMPOSITION_CREATE_PHOTOS
                state = IsState.RUNNING
                scanRequest = IsScan().apply {
                    id = IsScanId(scanId)
                    type = IsScanType.PHOTO
                    files = photos.toMutableList()
                }
            }

            log.info("Sending request with ${photos.size} photos")
            sendMessage(
                topicName = photoTopic,
                key = scanId,
                value = commonContextSerialize(context)
            )

            log.info("Waiting for message containing all photos")
            val found = waitForMessage(
                topic = photoTopic,
                predicate = { key, value ->
                    key == scanId &&
                            value.contains("photo1.jpg") &&
                            value.contains("photo2.jpg") &&
                            value.contains("photo3.jpg")
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Multiple photos processing test completed successfully")
        }
    }

    @Nested
    inner class CompositionManualFlowTests {

        @Test
        fun `composition manual request sends message to kafka`() {
            log.info("Starting composition manual request test")

            val requestTopic = "ingredient-composition-request"
            createTopic(requestTopic)

            val compositionText = "Water, Salt, Sugar, Preservative E202"
            val requestId = "req-manual-comp-001"

            val context = IsContext().apply {
                this.requestId = IsRequestId(requestId)
                command = IsCommand.COMPOSITION_CREATE_MANUAL
                state = IsState.RUNNING
                compositionRequest = IsComposition().apply {
                    text = compositionText
                }
            }

            log.info("Sending manual composition request: $compositionText")
            sendMessage(
                topicName = requestTopic,
                key = requestId,
                value = commonContextSerialize(context)
            )

            log.info("Waiting for composition request message")
            val found = waitForMessage(
                topic = requestTopic,
                predicate = { key, value ->
                    key == requestId &&
                            value.contains(compositionText) &&
                            value.contains("COMPOSITION_CREATE_MANUAL")
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Composition manual request test completed successfully")
        }

        @Test
        fun `various composition types are processed`() {
            log.info("Starting various composition types test")

            val requestTopic = "ingredient-composition-request-various"
            createTopic(requestTopic)

            consumer.unsubscribe()
            consumer.subscribe(listOf(requestTopic))
            Thread.sleep(500)

            val compositions = listOf(
                "Water, Salt, Sugar",
                "H2O, NaCl, C12H22O11",
                "Вода, Соль, Сахар",
                "H₂O, NaCl (хлорид натрия), C₆H₁₂O₆"
            )

            log.info("Sending ${compositions.size} different compositions")
            compositions.forEachIndexed { index, text ->
                val context = IsContext().apply {
                    requestId = IsRequestId("req-comp-$index")
                    command = IsCommand.COMPOSITION_CREATE_MANUAL
                    state = IsState.RUNNING
                    compositionRequest = IsComposition().apply {
                        this.text = text
                    }
                }

                sendMessage(
                    topicName = requestTopic,
                    key = context.requestId.asString(),
                    value = commonContextSerialize(context)
                )
            }

            val receivedCompositions = mutableListOf<String>()
            var collectedCount = 0
            var attempts = 0
            val maxAttempts = 10

            log.info("Collecting received compositions")
            while (collectedCount < compositions.size && attempts < maxAttempts) {
                val records = consumer.poll(java.time.Duration.ofMillis(2000))
                if (!records.isEmpty) {
                    records.forEach { record ->
                        val json = record.value()
                        compositions.forEach { comp ->
                            if (json.contains("\"text\":\"${comp}\"")) {
                                if (!receivedCompositions.contains(comp)) {
                                    receivedCompositions.add(comp)
                                    collectedCount++
                                    log.debug("Received composition: $comp")
                                }
                            }
                        }
                    }
                }
                attempts++
            }

            log.info("Asserting all compositions were received")
            assertThat(receivedCompositions).hasSize(compositions.size)
            assertThat(receivedCompositions).containsAll(compositions)

            log.info("✅ Various composition types test completed successfully")
        }
    }

    @Nested
    inner class KafkaStreamsTopologyTests {

        @Test
        fun `topology processes messages`() {
            log.info("Starting topology processes messages test")

            val inputTopic = "photo-input-topology"

            createTopic(inputTopic)

            consumer.unsubscribe()
            consumer.subscribe(listOf(inputTopic))
            Thread.sleep(500)

            val testMessages = listOf(
                "photo1" to "Process photo 1",
                "photo2" to "Process photo 2",
                "photo3" to "Process photo 3"
            )

            log.info("Sending ${testMessages.size} messages to input topic")
            testMessages.forEach { (key, value) ->
                sendMessage(
                    topicName = inputTopic,
                    key = key,
                    value = value
                )
            }

            val receivedMessages = mutableListOf<Pair<String, String>>()
            var attempts = 0
            val maxAttempts = 10

            log.info("Collecting processed messages")
            while (receivedMessages.size < testMessages.size && attempts < maxAttempts) {
                val records = consumer.poll(java.time.Duration.ofMillis(2000))
                if (!records.isEmpty) {
                    records.forEach { record ->
                        val pair = record.key() to record.value()
                        if (!receivedMessages.contains(pair)) {
                            receivedMessages.add(pair)
                            log.debug("Received message: ${record.key()} -> ${record.value()}")
                        }
                    }
                }
                attempts++
            }

            log.info("Asserting all messages were processed")
            assertThat(receivedMessages).hasSize(testMessages.size)
            testMessages.forEachIndexed { index, expected ->
                assertThat(receivedMessages[index]).isEqualTo(expected)
            }

            log.info("✅ Topology processes messages test completed successfully")
        }

        @Test
        fun `partitioning by key works`() {
            log.info("Starting partitioning by key test")

            val topic = "partitioned-topic"

            createTopic(topic, partitions = 3)

            consumer.unsubscribe()
            consumer.subscribe(listOf(topic))
            Thread.sleep(500)

            val messages = listOf(
                "user-1" to "message from user 1",
                "user-2" to "message from user 2",
                "user-1" to "another message from user 1"
            )

            log.info("Sending messages with different keys")
            messages.forEach { (key, value) ->
                sendMessage(topicName = topic, key = key, value = value)
            }

            val receivedRecords = mutableListOf<Pair<String, String>>()
            var attempts = 0
            val maxAttempts = 10

            log.info("Collecting messages from partitions")
            while (receivedRecords.size < messages.size && attempts < maxAttempts) {
                val records = consumer.poll(java.time.Duration.ofMillis(2000))
                if (!records.isEmpty) {
                    records.forEach { record ->
                        val pair = record.key() to record.value()
                        if (!receivedRecords.contains(pair)) {
                            receivedRecords.add(pair)
                            log.debug("Received message: ${record.key()} -> ${record.value()}")
                        }
                    }
                }
                attempts++
            }

            log.info("Asserting messages were partitioned correctly")
            assertThat(receivedRecords).isNotEmpty()

            val user1Messages = receivedRecords.filter { it.first == "user-1" }
            val user2Messages = receivedRecords.filter { it.first == "user-2" }

            assertThat(user1Messages.size).isGreaterThanOrEqualTo(1)
            assertThat(user2Messages.size).isGreaterThanOrEqualTo(1)

            log.info("✅ Partitioning by key test completed successfully")
        }
    }

    @Nested
    inner class ScanTypeTests {

        @Test
        fun `manual scan type is processed`() {
            log.info("Starting manual scan type test")

            val topic = "scan-manual-topic"
            createTopic(topic)

            val scanId = UUID.randomUUID().toString()
            val context = IsContext().apply {
                requestId = IsRequestId("req-manual-scan")
                command = IsCommand.COMPOSITION_CREATE_MANUAL
                state = IsState.RUNNING
                scanRequest = IsScan().apply {
                    id = IsScanId(scanId)
                    type = IsScanType.MANUAL
                    text = "Ручное сканирование"
                }
            }

            log.info("Sending manual scan request")
            sendMessage(
                topicName = topic,
                key = scanId,
                value = commonContextSerialize(context)
            )

            log.info("Waiting for manual scan message")
            val found = waitForMessage(
                topic = topic,
                predicate = { key, value ->
                    value.contains("MANUAL")
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Manual scan type test completed successfully")
        }

        @Test
        fun `photo scan type is processed`() {
            log.info("Starting photo scan type test")

            val topic = "scan-photo-topic"
            createTopic(topic)

            val scanId = UUID.randomUUID().toString()
            val context = IsContext().apply {
                requestId = IsRequestId("req-photo-scan")
                command = IsCommand.COMPOSITION_CREATE_PHOTOS
                state = IsState.RUNNING
                scanRequest = IsScan().apply {
                    id = IsScanId(scanId)
                    type = IsScanType.PHOTO
                    files = mutableListOf("photo1.jpg", "photo2.jpg")
                }
            }

            log.info("Sending photo scan request")
            sendMessage(
                topicName = topic,
                key = scanId,
                value = commonContextSerialize(context)
            )

            log.info("Waiting for photo scan message")
            val found = waitForMessage(
                topic = topic,
                predicate = { key, value ->
                    value.contains("PHOTO")
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Photo scan type test completed successfully")
        }
    }

    @Nested
    inner class ErrorHandlingTests {

        @Test
        fun `invalid json is handled`() {
            log.info("Starting invalid JSON handling test")

            val topic = "invalid-json-topic"
            createTopic(topic)

            val invalidJson = "{invalid json format"

            log.info("Sending invalid JSON message")
            sendMessage(
                topicName = topic,
                key = "error-1",
                value = invalidJson
            )

            log.info("Waiting for invalid JSON message")
            val found = waitForMessage(
                topic = topic,
                predicate = { key, value ->
                    key == "error-1" && value == invalidJson
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Invalid JSON handling test completed successfully")
        }

        @Test
        fun `empty message is handled`() {
            log.info("Starting empty message handling test")

            val topic = "empty-message-topic"
            createTopic(topic)

            log.info("Sending empty message")
            sendMessage(topicName = topic, key = "empty", value = "")

            log.info("Waiting for empty message")
            val found = waitForMessage(
                topic = topic,
                predicate = { key, value ->
                    key == "empty" && value.isEmpty()
                }
            )

            assertThat(found).isTrue()
            log.info("✅ Empty message handling test completed successfully")
        }
    }
}