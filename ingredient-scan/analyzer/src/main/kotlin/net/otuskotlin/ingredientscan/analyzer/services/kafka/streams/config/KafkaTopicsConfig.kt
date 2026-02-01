package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
@EnableConfigurationProperties(TopicConfigProperties::class)
open class KafkaTopicsConfig(private val topicProperties: TopicConfigProperties) {

    private val log = LoggerFactory.getLogger(KafkaTopicsConfig::class.java)

    companion object {
        const val COMPOSITION_CREATE_INPUT = "composition-create-input"
        const val COMPOSITION_OUTPUT = "composition-output"
        const val OCR_RECOGNITION_INPUT = "ocr-recognition-input"
        const val ANALYSIS_CREATE_INPUT = "analysis-create-input"
    }

    @Bean
    open fun kafkaAdmin(): KafkaAdmin {
        val configs = mutableMapOf<String, Any>(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to topicProperties.bootstrapServers,
            AdminClientConfig.CLIENT_ID_CONFIG to "ingredient-scan-admin"
        )
        return KafkaAdmin(configs)
    }

    @Bean
    open fun topic1() = NewTopic(COMPOSITION_CREATE_INPUT, topicProperties.partitions, topicProperties.replicationFactor.toShort())

    @Bean
    open fun topic2() = NewTopic(COMPOSITION_OUTPUT, topicProperties.partitions, topicProperties.replicationFactor.toShort())

    @Bean
    open fun topic3() = NewTopic(OCR_RECOGNITION_INPUT, topicProperties.partitions, topicProperties.replicationFactor.toShort())

    @Bean
    open fun topic4() = NewTopic(ANALYSIS_CREATE_INPUT, topicProperties.partitions, topicProperties.replicationFactor.toShort())
}
