package net.otuskotlin.ingredientscan.tests.e2e.be.base

import kotlinx.coroutines.runBlocking
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URI

@Testcontainers
abstract class BaseInfrastructureTest {

    companion object {
        val network: Network = Network.newNetwork()

        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17.7")
            .withDatabaseName("ingredient_scan")
            .withUsername("ingredient_user")
            .withPassword("ingredient_pass")
            .withNetwork(network)
            .withNetworkAliases("postgres")

        @Container
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withExposedPorts(9092, 9093)

        @Container
        val minio: GenericContainer<*> = GenericContainer("minio/minio:latest")
            .withExposedPorts(9000, 9001)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadminpassword")
            .withCommand("server /data --console-address :9001")
            .withNetwork(network)
            .withNetworkAliases("minio")
            .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000))
    }

    protected fun createMinioBucket() {
        val s3Client = S3Client.builder()
            .endpointOverride(URI.create("http://${minio.host}:${minio.getMappedPort(9000)}"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("minioadmin", "minioadminpassword")
                )
            )
            .region(Region.US_EAST_1)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()

        val bucketName = "photos"
        runBlocking {
            try {
                s3Client.headBucket { it.bucket(bucketName) }
                println("Bucket '$bucketName' already exists")
            } catch (e: S3Exception) {
                if (e.statusCode() == 404) {
                    s3Client.createBucket { it.bucket(bucketName) }
                    println("Bucket '$bucketName' created")
                } else {
                    throw e
                }
            }
        }
    }
}