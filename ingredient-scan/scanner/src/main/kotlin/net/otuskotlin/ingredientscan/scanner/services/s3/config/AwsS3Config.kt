package net.otuskotlin.ingredientscan.scanner.services.s3.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import java.net.URI

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
open class AwsS3Config(private val awsProperties: AwsProperties) {

    @Bean
    open fun s3AsyncClient(): S3AsyncClient {
        return S3AsyncClient.builder()
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
            .build()
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
    }

    class RegionProps {
        lateinit var static: String
    }
}
