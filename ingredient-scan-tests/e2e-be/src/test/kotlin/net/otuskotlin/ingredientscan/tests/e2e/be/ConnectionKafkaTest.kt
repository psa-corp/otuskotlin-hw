package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseKafkaTest
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class ConnectionKafkaTest : BaseKafkaTest() {

    @Test
    fun `check kafka connection`() {
        log.info("Starting Kafka connection check test")
        checkKafkaConnection()
        log.info("✅ Kafka connection check test completed successfully")
    }

    @Test
    fun `create topic`() {
        log.info("Starting create topic test")
        val testTopicName = "test-create-topic"
        createTopic(testTopicName)
        log.info("✅ Create topic test completed successfully")
    }

    @Test
    fun `send and receive message`() {
        log.info("Starting send and receive message test")

        val testTopic = "test-send-receive"
        val testKey = "key-1"
        val testMessage = "Hello, Kafka!"

        createTopic(testTopic)
        log.info("Created topic: $testTopic")

        log.info("Sending message to topic: $testTopic")
        sendMessage(topicName = testTopic, key = testKey, value = testMessage)

        log.info("Consuming message from topic: $testTopic")
        val result = consumeMessage(topicName = testTopic)

        assertThat(result).isNotNull
        val (key, value) = result!!

        assertThat(key).isEqualTo(testKey)
        assertThat(value).isEqualTo(testMessage)

        log.info("✅ Send and receive message test completed successfully")
    }

    @Test
    fun `send multiple messages`() {
        log.info("Starting send multiple messages test")

        val testTopic = "test-multiple-messages"
        val messages = listOf("Message 1", "Message 2", "Message 3")

        createTopic(testTopic)
        log.info("Created topic: $testTopic")

        log.info("Sending ${messages.size} messages to topic")
        messages.forEach { msg ->
            sendMessage(topicName = testTopic, value = msg)
        }

        consumer.unsubscribe()
        consumer.subscribe(listOf(testTopic))

        val receivedMessages = mutableListOf<Pair<String?, String>>()
        var collectedCount = 0
        var attempts = 0
        val maxAttempts = 10

        log.info("Consuming messages from topic")
        while (collectedCount < messages.size && attempts < maxAttempts) {
            val records = consumer.poll(java.time.Duration.ofMillis(2000))
            if (!records.isEmpty) {
                records.forEach { record ->
                    receivedMessages.add(Pair(record.key(), record.value()))
                    collectedCount++
                    log.debug("Received message #$collectedCount: ${record.value()}")
                }
            }
            attempts++
        }

        log.info("Asserting all messages were received")
        assertThat(receivedMessages).hasSize(messages.size)
        messages.forEachIndexed { index, expectedValue ->
            assertThat(receivedMessages[index].second).isEqualTo(expectedValue)
        }

        log.info("✅ Send multiple messages test completed successfully")
    }

    @Test
    fun `send message without key`() {
        log.info("Starting send message without key test")

        val testTopic = "test-no-key"
        val testMessage = "Message without key"

        createTopic(testTopic)
        log.info("Created topic: $testTopic")

        log.info("Sending message without key")
        sendMessage(topicName = testTopic, key = null, value = testMessage)

        val result = consumeMessage(topicName = testTopic)

        assertThat(result).isNotNull
        assertThat(result!!.second).isEqualTo(testMessage)

        log.info("✅ Send message without key test completed successfully")
    }

    @Test
    fun `clear topic`() {
        log.info("Starting clear topic test")

        val testTopic = "test-clear-topic"

        createTopic(testTopic)
        log.info("Created topic: $testTopic")

        log.info("Sending test messages to topic")
        sendMessage(topicName = testTopic, value = "Message 1")
        sendMessage(topicName = testTopic, value = "Message 2")

        log.info("Clearing topic")
        clearTopic(testTopic)

        val result = consumeMessage(topicName = testTopic, timeoutMs = 1000)
        assertThat(result).isNull()

        log.info("✅ Clear topic test completed successfully")
    }
}