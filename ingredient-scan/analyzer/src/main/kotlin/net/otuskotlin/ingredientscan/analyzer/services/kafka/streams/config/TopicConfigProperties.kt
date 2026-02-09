package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class TopicConfigProperties(
    var partitions: Int = 1,
    var replicationFactor: Int = 1,
    var bootstrapServers: String = "localhost:9092",
    var autoCreate: Boolean = true
)