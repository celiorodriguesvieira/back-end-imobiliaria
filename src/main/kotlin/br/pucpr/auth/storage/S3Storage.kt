package br.pucpr.auth.storage

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Component
open class S3Storage(
    private val s3: S3Client,
    private val props: S3Properties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    open fun upload(key: String, bytes: ByteArray, contentType: String): String {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(bytes),
        )
        val url = publicUrl(key)
        log.info("Objeto enviado ao S3: {}", url)
        return url
    }

    open fun delete(key: String) {
        s3.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(props.bucket)
                .key(key)
                .build()
        )
        log.info("Objeto removido do S3: {}", key)
    }

    private fun publicUrl(key: String): String {
        val endpoint = props.endpoint
        return if (endpoint.isNullOrBlank()) {
            "https://${props.bucket}.s3.${props.region}.amazonaws.com/$key"
        } else {
            "${endpoint.trimEnd('/')}/${props.bucket}/$key"
        }
    }
}
