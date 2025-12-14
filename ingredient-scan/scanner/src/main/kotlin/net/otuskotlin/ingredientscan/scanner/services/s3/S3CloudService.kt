package net.otuskotlin.ingredientscan.scanner.services.s3

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import jakarta.annotation.PostConstruct
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.IOException
import java.util.UUID


@Service
class S3CloudService(
    private val s3Template: S3Template,
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

    fun uploadFiles(context: IsContext, files: Array<MultipartFile>, prefix: String?): MutableList<String> {
        val result = mutableListOf<String>()

        if (files.isEmpty()) {
            context.errors.add(createError("NO_FILES", "No files provided"))
            return result
        }

        if (files.size > maxFiles) {
            context.errors.add(createError("TOO_MANY_FILES", "Too many files: max $maxFiles allowed"))
            return result
        }

        for (file in files) {
            val fileName = uploadFile(context, file, prefix)
            if (fileName == null) {
                break
            }
            result.add(fileName)
        }

        return result
    }

    fun uploadFile(context: IsContext, file: MultipartFile, prefix: String?): String? {
        val finalPrefix = if (StringUtils.hasLength(prefix)) "$prefix/" else ""
        val fileName = finalPrefix + UUID.randomUUID() + "_" + file.originalFilename

        if (fileExists(fileName)) {
            context.errors.add(createError("FILE_EXISTS", "File already exists: $fileName"))
            return null
        }

        val metadata = ObjectMetadata.builder()
            .contentType(file.contentType)
            .build()

        try {
            s3Template.upload(bucketName, fileName, file.inputStream, metadata)
            return fileName
        } catch (e: IOException) {
            context.errors.add(createError("STORE_NOT_FOUND", "Storage not found or unavailable: $fileName"))
            return null
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