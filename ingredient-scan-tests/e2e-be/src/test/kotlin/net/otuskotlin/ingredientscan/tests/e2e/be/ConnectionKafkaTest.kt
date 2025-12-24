package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseKafkaTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

@DisplayName("Kafka Connection Tests")
class ConnectionKafkaTest : BaseKafkaTest() {

    @Test
    @DisplayName("✅ Проверка подключения к Kafka")
    fun testKafkaConnection() {
        checkKafkaConnection()
    }

    @Test
    @DisplayName("✅ Создание топика")
    fun testCreateTopic() {
        val testTopicName = "test-create-topic"
        createTopic(testTopicName)
        log.info("✅ Тест пройден: топик успешно создан")
    }

    @Test
    @DisplayName("✅ Отправка и получение сообщения")
    fun testSendAndReceiveMessage() {
        val testTopic = "test-send-receive"
        val testKey = "key-1"
        val testMessage = "Hello, Kafka!"

        createTopic(testTopic)
        sendMessage(topicName = testTopic, key = testKey, value = testMessage)

        val result = consumeMessage(topicName = testTopic)

        assertThat(result).isNotNull
        val (key, value) = result!!

        assertThat(key).isEqualTo(testKey)
        assertThat(value).isEqualTo(testMessage)
        log.info("✅ Тест пройден: сообщение успешно отправлено и получено")
    }

    @Test
    @DisplayName("✅ Несколько сообщений подряд")
    fun testMultipleMessages() {
        val testTopic = "test-multiple-messages"
        val messages = listOf("Message 1", "Message 2", "Message 3")

        createTopic(testTopic)
        messages.forEach { msg ->
            sendMessage(topicName = testTopic, value = msg)
        }

        log.info("📤 Отправлено ${messages.size} сообщений")

        // ✅ Очищаем Consumer и переподписываемся
        consumer.unsubscribe()
        consumer.subscribe(listOf(testTopic))

        val receivedMessages = mutableListOf<Pair<String?, String>>()
        var collectedCount = 0
        var attempts = 0
        val maxAttempts = 10

        // ✅ Собираем сообщения в цикле, пока не получим все 3
        while (collectedCount < messages.size && attempts < maxAttempts) {
            val records = consumer.poll(java.time.Duration.ofMillis(2000))
            if (!records.isEmpty) {
                records.forEach { record ->
                    receivedMessages.add(Pair(record.key(), record.value()))
                    collectedCount++
                    log.info("✅ Получено сообщение #$collectedCount: ${record.value()}")
                }
            }
            attempts++
        }

        assertThat(receivedMessages).hasSize(messages.size)
        messages.forEachIndexed { index, expectedValue ->
            assertThat(receivedMessages[index].second).isEqualTo(expectedValue)
        }

        log.info("✅ Тест пройден: получено ${receivedMessages.size} сообщений")
    }
    @Test
    @DisplayName("✅ Отправка без ключа")
    fun testSendMessageWithoutKey() {
        val testTopic = "test-no-key"
        val testMessage = "Message without key"

        createTopic(testTopic)
        sendMessage(topicName = testTopic, key = null, value = testMessage)

        val result = consumeMessage(topicName = testTopic)

        assertThat(result).isNotNull
        assertThat(result!!.second).isEqualTo(testMessage)
        log.info("✅ Тест пройден: сообщение без ключа успешно обработано")
    }

    @Test
    @DisplayName("✅ Очистка топика")
    fun testClearTopic() {
        val testTopic = "test-clear-topic"

        createTopic(testTopic)
        sendMessage(topicName = testTopic, value = "Message 1")
        sendMessage(topicName = testTopic, value = "Message 2")
        log.info("📤 Отправлено 2 сообщения")

        clearTopic(testTopic)

        val result = consumeMessage(topicName = testTopic, timeoutMs = 1000)

        assertThat(result).isNull()
        log.info("✅ Тест пройден: топик успешно очищен")
    }
}
