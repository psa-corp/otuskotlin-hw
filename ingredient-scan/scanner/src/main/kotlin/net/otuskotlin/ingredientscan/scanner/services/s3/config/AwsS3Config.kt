package net.otuskotlin.ingredientscan.scanner.services.s3.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.transfer.s3.S3TransferManager
import java.net.URI

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
open class AwsS3Config(private val awsProperties: AwsProperties) {
    private val log = LoggerFactory.getLogger(AwsS3Config::class.java)

    @Bean
    open fun s3AsyncClient(): S3AsyncClient {
        return S3AsyncClient.crtBuilder()
            .endpointOverride(URI.create(awsProperties.s3.endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsProperties.credentials.accessKey,
                        awsProperties.credentials.secretKey
                    )
                )
            )
            .region(Region.of(awsProperties.region.static))
            .forcePathStyle(true)
            .build()
    }

    @Bean
    open fun s3TransferManager(s3AsyncClient: S3AsyncClient): S3TransferManager {
        return S3TransferManager.builder()
            .s3Client(s3AsyncClient)
            .build()
    }

    @Bean
    open fun bucketInitializer(s3AsyncClient: S3AsyncClient): CommandLineRunner {
        return CommandLineRunner {
            val bucketName = awsProperties.s3.bucket.name

            Mono.fromFuture { s3AsyncClient.headBucket { it.bucket(bucketName) } }
                .then()
                .onErrorResume { ex ->
                    if (ex.cause is NoSuchBucketException || ex is NoSuchBucketException) {
                        log.error("Bucket {} not found, creating...", bucketName)
                        Mono.fromFuture { s3AsyncClient.createBucket { it.bucket(bucketName) } }.then()
                    } else {
                        Mono.error(ex)
                    }
                }
                .doOnError { log.error("Error during bucket initialization:{}", it.message) }
                .subscribe() // Используем subscribe, так как это CommandLineRunner
        }
    }
}

@ConfigurationProperties(prefix = "spring.cloud.aws")
class AwsProperties {
    lateinit var credentials: Credentials
    lateinit var region: RegionProps
    lateinit var s3: S3Props

    class Credentials {
        lateinit var accessKey: String
        lateinit var secretKey: String
    }

    class S3Props {
        lateinit var endpoint: String
        lateinit var bucket: S3BucketProps
    }

    class RegionProps {
        lateinit var static: String
    }

    class S3BucketProps {
        lateinit var name: String
    }
}
