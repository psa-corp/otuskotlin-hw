package net.otuskotlin.ingredientscan.scanner.services.s3

import io.awspring.cloud.s3.S3Template
import jakarta.annotation.PostConstruct
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.nio.ByteBuffer
import java.util.*


@Service
class S3CloudService(
    private val s3Template: S3Template,
    private val s3AsyncClient: S3AsyncClient,
    private val s3Client: S3Client,
    @Value("\${spring.cloud.aws.s3.bucket.name:photos}")
    private val bucketName: String,
    @Value("\${app.upload.max-files:5}")
    private val maxFiles: Int
) {

    private val log = LoggerFactory.getLogger(S3CloudService::class.java)

    @PostConstruct
    fun initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())
            log.info("Bucket '{}' already exists", bucketName)
        } catch (e: Exception) {
            try {
                log.info("Creating bucket '{}'...", bucketName)
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                log.info("Bucket '{}' created successfully", bucketName)
            } catch (createEx: Exception) {
                log.warn("Could not create bucket '{}': {}", bucketName, createEx.message)
            }
        }
    }

    companion object {

        fun chunks(path: String, filename: String, hasLength: Boolean): Array<String> {
            val p = if (hasLength) filename.substring(path.length) else filename
            return p.split("/").toTypedArray()
        }
    }

    fun uploadFiles(context: IsContext, files: Flux<FilePart>, prefix: String?): Mono<List<String>> {

        return files
            .index()
            .flatMap { tuple ->
                val index = tuple.t1
                val file = tuple.t2

                if (index >= maxFiles) {
                    context.errors.add(
                        createError(
                            "TOO_MANY_FILES",
                            "Too many files: max $maxFiles allowed"
                        )
                    )
                    return@flatMap Mono.error(IllegalArgumentException("Too many files"))
                }

                uploadFile(context, file, prefix)
            }
            .switchIfEmpty(
                Mono.defer {
                    context.errors.add(
                        createError("NO_FILES", "No files provided")
                    )
                    Mono.error(IllegalArgumentException("No files"))
                }
            )
            .collectList()
    }

    fun uploadFile(context: IsContext, file: FilePart, prefix: String?): Mono<String> {
        val finalPrefix = if (StringUtils.hasText(prefix)) "$prefix/" else ""
        val fileName = finalPrefix + UUID.randomUUID() + "_" + file.filename()

        return Mono.fromCallable { fileExists(fileName) }
            .flatMap { exists ->
                if (exists) {
                    context.errors.add(
                        createError("FILE_EXISTS", "File already exists: $fileName")
                    )
                    Mono.error(IllegalStateException("File exists"))
                } else {

                    val request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.headers().contentType?.toString())
                        .build()

                    val body = AsyncRequestBody.fromPublisher(
                        file.content().map { buffer ->
                            try {
                                val bytes = ByteArray(buffer.readableByteCount())
                                buffer.read(bytes)
                                ByteBuffer.wrap(bytes)
                            } finally {
                                DataBufferUtils.release(buffer)
                            }
                        }
                    )

                    Mono.fromFuture(
                        s3AsyncClient.putObject(request, body)
                    ).thenReturn(fileName)
                }
            }
            .onErrorResume { ex ->
                context.errors.add(
                    createError(
                        "STORE_NOT_FOUND",
                        "Storage not found or unavailable: $fileName"
                    )
                )
                Mono.error(ex)
            }
    }

    fun getObjectMetadata(context: IsContext, fileName: String): HeadObjectResponse? {
        try {
            val request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()

            return s3Client.headObject(request)
        } catch (e: S3Exception) {
            context.errors.add(createError("STORE_NOT_FOUND", "Storage not found or unavailable: $fileName"))
            return null
        }
    }

    fun fileExists(fileKey: String): Boolean {
        return s3Template.objectExists(bucketName, fileKey)
    }

    fun downloadFileAsResource(context: IsContext, fileName: String): Resource? {
        try {
            if (!fileExists(fileName)) {
                context.errors.add(createError("FILE_NOT_FOUND", "File not found: $fileName"))
                return null
            }

            return s3Template.download(bucketName, fileName)
        } catch (e: Exception) {
            context.errors.add(createError("STORE_NOT_FOUND", "Storage not found or unavailable: $fileName"))
            return null
        }
    }

    fun deleteFile(context: IsContext, fileName: String): Boolean {
        try {
            if (!fileExists(fileName)) {
                context.errors.add(createError("FILE_NOT_FOUND", "File not found: $fileName"))
                return false
            }

            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build()
            )
            return true
        } catch (e: Exception) {
            context.errors.add(createError("STORE_NOT_FOUND", "Storage not found or unavailable: $fileName"))
            return false
        }
    }

    private fun createError(code: String, message: String): IsError {
        return IsError(
            code = code,
            group = "s3",
            field = "",
            message = message
        )
    }
}