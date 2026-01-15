package net.otuskotlin.ingredientscan.tests.e2e.be.base

import net.otuskotlin.ingredientscan.tests.e2e.be.TestApplication
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.Properties
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
    properties = [
        "spring.docker.compose.enabled=false",
        "spring.docker.compose.skip.in-tests=true",
        "spring.main.web-application-type=none",
        "spring.kafka.bootstrap-servers=localhost:9092"
    ]
)
abstract class BaseKafkaTest {

    protected val log = LoggerFactory.getLogger(this::class.java)

    protected lateinit var producer: KafkaProducer<String, String>
    protected lateinit var consumer: KafkaConsumer<String, String>
    protected lateinit var adminClient: AdminClient

    companion object {
        const val KAFKA_BROKER = "localhost:9092"
        const val TEST_TOPIC = "test.topic"
        const val TEST_GROUP = "test-consumer-group"
    }

    @BeforeEach
    fun setupKafkaClients() {
        initializeKafkaClients()
    }

    protected fun initializeKafkaClients() {
        log.info("🔌 Initializing Kafka clients: $KAFKA_BROKER")
        producer = KafkaProducer(createProducerProperties())
        consumer = KafkaConsumer(createConsumerProperties())
        adminClient = AdminClient.create(createAdminProperties())
        log.info("✅ Kafka clients initialized")
    }

    @AfterEach
    fun cleanupKafkaClients() {
        log.info("🧹 Closing Kafka clients")
        try {
            if (this::producer.isInitialized) producer.close()
            if (this::consumer.isInitialized) consumer.close()
            if (this::adminClient.isInitialized) adminClient.close()
        } catch (e: Exception) {
            log.warn("⚠️ Error closing Kafka clients", e)
        }
    }

    protected fun createTopic(
        topicName: String = TEST_TOPIC,
        partitions: Int = 1,
        replicationFactor: Short = 1
    ) {
        log.info("📝 Creating topic: $topicName")
        try {
            try {
                adminClient.deleteTopics(listOf(topicName)).all().get(10, TimeUnit.SECONDS)
                log.info("🗑️ Old topic deleted: $topicName")
                Thread.sleep(500)
            } catch (e: Exception) {
                log.debug("ℹ️ Topic did not exist")
            }

            val newTopic = NewTopic(topicName, partitions, replicationFactor)
            adminClient.createTopics(listOf(newTopic)).all().get(10, TimeUnit.SECONDS)
            log.info("✅ Topic created: $topicName")
            Thread.sleep(500)
        } catch (e: Exception) {
            log.error("❌ Error creating topic: $topicName", e)
            throw RuntimeException("Failed to create topic: $topicName", e)
        }
    }

    protected fun sendMessage(
        topicName: String = TEST_TOPIC,
        key: String? = null,
        value: String
    ): String {
        log.info("📤 Sending message to $topicName")
        log.debug("   Key: $key, Value: $value")
        val record = ProducerRecord(topicName, key, value)
        val metadata = producer.send(record).get(10, TimeUnit.SECONDS)
        log.info("✅ Message sent (offset=${metadata.offset()}, partition=${metadata.partition()})")
        return metadata.offset().toString()
    }

    protected fun consumeMessage(
        topicName: String = TEST_TOPIC,
        timeoutMs: Long = 5000
    ): Pair<String?, String>? {
        log.info("📥 Receiving message from $topicName (timeout=${timeoutMs}ms)")
        consumer.subscribe(listOf(topicName))
        val records = consumer.poll(java.time.Duration.ofMillis(timeoutMs))
        if (records.isEmpty) {
            log.warn("⚠️ No messages in topic $topicName")
            return null
        }

        val record = records.first()
        log.info("✅ Message received (key=${record.key()}, offset=${record.offset()})")
        log.debug("   Value: ${record.value()}")
        return Pair(record.key(), record.value())
    }

    protected fun checkKafkaConnection() {
        log.info("🔍 Checking Kafka connection: $KAFKA_BROKER")
        try {
            val nodes = adminClient.describeCluster().nodes().get(10, TimeUnit.SECONDS)
            log.info("✅ Connection successful! Nodes: ${nodes.size}")
            nodes.forEach { node ->
                log.debug("   - Node ${node.id()}: ${node.host()}:${node.port()}")
            }
        } catch (e: Exception) {
            log.error("❌ Kafka connection error", e)
            throw RuntimeException("Failed to connect to Kafka at $KAFKA_BROKER", e)
        }
    }

    protected fun clearTopic(topicName: String = TEST_TOPIC) {
        log.info("🧹 Clearing topic: $topicName")
        val cleanConsumer: KafkaConsumer<String, String> = KafkaConsumer(
            createConsumerProperties().apply {
                put(ConsumerConfig.GROUP_ID_CONFIG, "${TEST_GROUP}-clean-${System.currentTimeMillis()}")
            }
        )
        try {
            cleanConsumer.subscribe(listOf(topicName))
            var hasMore = true
            while (hasMore) {
                val records = cleanConsumer.poll(java.time.Duration.ofMillis(100))
                if (records.isEmpty) hasMore = false
            }
            log.info("✅ Topic cleared: $topicName")
        } finally {
            cleanConsumer.close()
        }
    }

    private fun createProducerProperties(): Properties {
        return Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 40000)
            put(ProducerConfig.LINGER_MS_CONFIG, 5)
            put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }
    }

    private fun createConsumerProperties(): Properties {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER)
            put(ConsumerConfig.GROUP_ID_CONFIG, TEST_GROUP)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000)
            put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000)
        }
    }

    private fun createAdminProperties(): Properties {
        return Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER)
            put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000)
        }
    }
}