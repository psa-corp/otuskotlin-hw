package net.otuskotlin.ingredientscan.tests.e2e.be

import net.otuskotlin.ingredientscan.api.v1.external.models.*
import net.otuskotlin.ingredientscan.tests.e2e.be.base.ApiClient
import net.otuskotlin.ingredientscan.tests.e2e.be.base.BaseInfrastructureTest
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class ControllerTest : BaseInfrastructureTest() {

    private val log = LoggerFactory.getLogger(ControllerTest::class.java)

    @Test
    fun `composition via manual input`() {
        createMinioBucket()
        createKafkaTopics()
        waitForKafkaReady()

        log.info("Starting scanner container...")
        val scanner = GenericContainer("darthchain/ingredient-scan-scan-hw:latest")
            .withExposedPorts(8080)
            .withNetwork(network)
            .withNetworkAliases("scanner")
            .withEnv("SPRING_PROFILES_ACTIVE", "prod")
            .withEnv("SPRING_BOOT_DOCKER_COMPOSE_ENABLED", "false")
            .withEnv("SPRING_R2DBC_URL", "r2dbc:postgresql://postgres:5432/ingredient_scan")
            .withEnv("SPRING_R2DBC_USERNAME", "ingredient_user")
            .withEnv("SPRING_R2DBC_PASSWORD", "ingredient_pass")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/ingredient_scan")
            .withEnv("SPRING_DATASOURCE_USERNAME", "ingredient_user")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "ingredient_pass")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("APP_KAFKA_TOPICS_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("SPRING_CLOUD_AWS_ENDPOINT", "http://minio:9000")
            .withEnv("SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY", "minioadmin")
            .withEnv("SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY", "minioadminpassword")
            .withEnv("SPRING_CLOUD_AWS_REGION_STATIC", "us-east-1")
            .withEnv("SPRING_CLOUD_AWS_S3_BUCKET_NAME", "photos")
            .withEnv("INTERNAL_API_TOKEN", "test-token-123")
            .withEnv("KEYCLOAK_ENABLED", "false")
            .withEnv("SPRING_KAFKA_CONSUMER_GROUP_ID", "test-scanner-group")
            .withEnv("SPRING_KAFKA_ADMIN_PROPERTIES_REQUEST_TIMEOUT_MS", "30000")
            .withEnv("SPRING_KAFKA_PROPERTIES_REQUEST_TIMEOUT_MS", "30000")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(3)))

        log.info("Starting analyzer container...")
        val analyzer = GenericContainer("darthchain/ingredient-scan-analyzer-hw:latest")
            .withExposedPorts(8080)
            .withNetwork(network)
            .withNetworkAliases("analyzer")
            .withEnv("SPRING_PROFILES_ACTIVE", "prod")
            .withEnv("SPRING_BOOT_DOCKER_COMPOSE_ENABLED", "false")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("APP_KAFKA_TOPICS_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("SPRING_CLOUD_AWS_ENDPOINT", "http://minio:9000")
            .withEnv("SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY", "minioadmin")
            .withEnv("SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY", "minioadminpassword")
            .withEnv("INTERNAL_API_TOKEN", "test-token-123")
            .withEnv("SCANNER_INTERNAL_URL", "http://scanner:8080/v1")
            .withEnv("SPRING_KAFKA_CONSUMER_GROUP_ID", "test-analyzer-group")
            .withEnv("LOGGING_LEVEL_NET_OTUSKOTLIN_INGREDIENTSCAN", "DEBUG")
            .withEnv("LOGGING_LEVEL_ORG_APACHE_KAFKA", "DEBUG")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(3)))

        scanner.start()
        analyzer.start()

        log.info("Analyzer logs after start:")
        log.info(analyzer.logs)

        Thread.sleep(15000)

        val apiClient = ApiClient(
            baseUrl = "http://${scanner.host}:${scanner.getMappedPort(8080)}",
            connectTimeout = Duration.ofSeconds(310),
            requestTimeout = Duration.ofSeconds(310)
        )

        try {
            val request = CompositionCreateByManualRequest(
                requestType = "compositionCreateByManual",
                scan = ScanManualDto(
                    text = "молоко, сахар, консервант E202",
                    type = ScanType.MANUAL
                )
            )

            val response: CompositionCreateByManualResponse = apiClient.post(request, "/v1/composition/create/manual")

            assertThat(response.result).isEqualTo(ResponseResult.SUCCESS)
            assertThat(response.contextId).isNotEmpty()
            assertThat(response.composition).isNotNull
            assertThat(response.composition?.id).isNotNull
            assertThat(response.composition?.text).contains("молоко")

            val request2 = CompositionGetRequest(
                requestType = "compositionGet",
                compositionId = response.composition?.id.toString()
            )
            val response2: CompositionGetResponse = apiClient.post(request2, "/v1/composition/get")
            assertThat(response2.result).isEqualTo(ResponseResult.SUCCESS)
            assertThat(response2.contextId).isNotEmpty()
            assertThat(response2.composition).isNotNull
            assertThat(response2.composition?.id).isNotNull
            assertThat(response2.composition?.id).isEqualTo(response.composition?.id)

            val request3 = AnalysisCreateRequest(
                requestType = "analysisCreate",
                compositionId = response.composition?.id.toString()
            )
            val response3: AnalysisCreateResponse = apiClient.post(request3, "/v1/analysis/create")
            assertThat(response3.result).isEqualTo(ResponseResult.SUCCESS)
//            assertThat(response3.contextId).isNotEmpty()
            assertThat(response3.analysis).isNotNull
            assertThat(response3.analysis?.id).isNotNull
            assertThat(response3.analysis?.compositionId).isEqualTo(response.composition?.id)

            val request4 = AnalysisGetRequest(
                requestType = "analysisGet",
                analysisId = response3.analysis?.id.toString()
            )
            val response4: AnalysisGetResponse = apiClient.post(request4, "/v1/analysis/get")
            assertThat(response4.result).isEqualTo(ResponseResult.SUCCESS)
//            assertThat(response3.contextId).isNotEmpty()
            assertThat(response4.analysis).isNotNull
            assertThat(response4.analysis?.id).isNotNull
            assertThat(response4.analysis?.id).isEqualTo(response3.analysis?.id)


        } catch (e: Exception) {
            log.error("Test failed, dumping container logs...")
            log.error("Scanner logs:\n${scanner.logs}")
            log.error("Analyzer logs:\n${analyzer.logs}")
            throw e
        }
    }

    private fun createKafkaTopics() {
        val bootstrapServers = kafka.bootstrapServers
        log.info("Connecting to Kafka at $bootstrapServers")
        val props = Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            // можно убрать явный порт
        }
        AdminClient.create(props).use { admin ->
            val topics = listOf(
                NewTopic("composition-create-input", 1, 1.toShort()),
                NewTopic("composition-output", 1, 1.toShort()),
                NewTopic("ocr-recognition-input", 1, 1.toShort()),
                NewTopic("analysis-create-input", 1, 1.toShort()),
            )
            admin.createTopics(topics).all().get(30, TimeUnit.SECONDS)
            val topicNames = admin.listTopics().names().get()
            log.info("Existing Kafka topics: $topicNames")
        }
    }

    private fun waitForKafkaReady() {
        log.info("Waiting for Kafka to be fully ready...")
        val props = Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:${kafka.getMappedPort(9093)}")
            put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000)
        }
        var retries = 30
        while (retries-- > 0) {
            try {
                AdminClient.create(props).use { admin ->
                    admin.listTopics().names().get(5, TimeUnit.SECONDS)
                    log.info("Kafka is ready")
                    return
                }
            } catch (e: Exception) {
                log.info("Waiting for Kafka... ${e.message}")
                Thread.sleep(2000)
            }
        }
        throw IllegalStateException("Kafka not ready after timeout")
    }
}