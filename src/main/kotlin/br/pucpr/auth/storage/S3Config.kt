package br.pucpr.auth.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@ConfigurationProperties(prefix = "aws")
data class S3Properties(
    val region: String = "us-east-1",
    val bucket: String = "",
    val accessKey: String? = null,
    val secretKey: String? = null,
    val endpoint: String? = null,
)

@Configuration
@EnableConfigurationProperties(S3Properties::class)
open class S3Config {

    @Bean
    open fun s3Client(props: S3Properties): S3Client {
        val builder = S3Client.builder().region(Region.of(props.region))

        if (!props.accessKey.isNullOrBlank() && !props.secretKey.isNullOrBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKey, props.secretKey)
                )
            )
        }

        if (!props.endpoint.isNullOrBlank()) {
            builder.endpointOverride(URI.create(props.endpoint))
            builder.forcePathStyle(true)
        }

        return builder.build()
    }
}
