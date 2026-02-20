package net.otuskotlin.ingredientscan.scanner.services.s3

import kotlinx.coroutines.reactor.awaitSingle
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorCustom
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsContentProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.UploadRequest


import java.nio.ByteBuffer
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Service
class S3CloudService(
    private val s3AsyncClient: S3AsyncClient,
    private val s3TransferManager: S3TransferManager,
    @Value("\${spring.cloud.aws.s3.bucket.name:photos}")
    private val bucketName: String,
    @Value("\${app.upload.max-files:5}")
    private val maxFiles: Int
) : IsContentProvider {

    private val log = LoggerFactory.getLogger(S3CloudService::class.java)

    override suspend fun upload(context: IsContext, files: Any, prefix: String?): List<String> {
        @Suppress("UNCHECKED_CAST")
        val fileParts = files as? Flux<FilePart>
            ?: throw IllegalArgumentException("Expected Flux<FilePart>, got ${files::class.simpleName}")

        return uploadFiles(context, fileParts, prefix).awaitSingle()
    }

    suspend fun uploadFiles(context: IsContext, files: Flux<FilePart>, prefix: String?): Mono<List<String>> {
        return files
            .take(maxFiles.toLong() + 1)
            .index()
            .flatMap { tuple ->
                if (tuple.t1 >= maxFiles) {
                    context.fail(
                        errorCustom(
                            code = "tooManyFiles",
                            field = "tooManyFiles",
                            group = "s3",
                            message = "Too many files: max $maxFiles allowed"
                        )
                    )
                    Mono.error(IllegalArgumentException("Too many files"))
                } else {
                    uploadFile(context, tuple.t2, prefix)
                }
            }
            .switchIfEmpty(
                Mono.defer {
                    context.fail(
                        errorCustom(
                            code = "noFiles",
                            field = "noFiles",
                            group = "s3",
                            message = "No files provided"
                        )
                    )
                    Mono.error(IllegalArgumentException("No files"))
                }
            )
            .collectList()
    }

    fun uploadFile(context: IsContext, file: FilePart, prefix: String?): Mono<String> {
        val finalPrefix = if (StringUtils.hasText(prefix)) "$prefix/" else ""
        val fileName = finalPrefix + UUID.randomUUID() + "_" + file.filename()
        return fileExistsMono(fileName)
            .flatMap { exists ->
                if (exists) {
                    context.fail(
                        errorCustom(
                            code = "fileExists",
                            field = "fileExists",
                            group = "s3",
                            message ="File already exists: $fileName"
                        )
                    )
                    Mono.error(IllegalStateException("File exists"))
                } else {

                    val byteBufferFlux = file.content().map { buffer ->
                        val replica = ByteBuffer.allocate(buffer.readableByteCount())
                        replica.put(buffer.asByteBuffer())
                        replica.flip()
                        DataBufferUtils.release(buffer)
                        replica
                    }

                    val asyncRequestBody = AsyncRequestBody.fromPublisher(byteBufferFlux)

                    val uploadRequest = UploadRequest.builder()
                        .putObjectRequest { b ->
                            b.bucket(bucketName)
                                .key(fileName)
                                .contentType(file.headers().contentType?.toString() ?: "application/octet-stream")
                        }
                        .requestBody(asyncRequestBody)
                        .build()

                    Mono.fromFuture(s3TransferManager.upload(uploadRequest).completionFuture())
                    .thenReturn(fileName)
                    .onErrorResume { ex ->
                        context.fail(
                            errorCustom(
                                code = "s3Error",
                                field = "s3Error",
                                group = "s3",
                                message = ex.message ?: "Unknown S3 error"
                            )
                        )
                        Mono.error(ex)
                    }
                }
            }
            .onErrorResume { ex ->
                if (ex is IllegalStateException) {
                    Mono.error(ex)
                } else {
                    context.fail(
                        errorCustom(
                            code = "storageNotFound",
                            field = "storageNotFound",
                            group = "s3",
                            message = "Storage not found or unavailable: $fileName"
                        )
                    )
                    Mono.error(ex)
                }
            }
    }

    fun fileExistsMono(fileKey: String): Mono<Boolean> {
        return Mono.fromFuture {
            s3AsyncClient.headObject { b ->
                b.bucket(bucketName).key(fileKey)
            }
        }
        .map { true }
        .onErrorResume { ex ->
            if (ex.cause is NoSuchKeyException || ex is NoSuchKeyException) {
                Mono.just(false)
            } else {
                Mono.error(ex)
            }
        }
    }

    override suspend fun download(context: IsContext) : Any {
        return downloadPhotosAsZip(context.files)
    }

    fun downloadPhotosAsZip(fileNames: List<String>): Flux<DataBuffer> {
        return Flux.create { emitter ->
            val bufferFactory = DefaultDataBufferFactory.sharedInstance

            val os = object : java.io.OutputStream() {
                override fun write(b: Int) = write(byteArrayOf(b.toByte()), 0, 1)
                override fun write(b: ByteArray, off: Int, len: Int) {
                    emitter.next(bufferFactory.wrap(b.copyOfRange(off, off + len)))
                }
            }
            val zos = ZipOutputStream(os)

            getPhotos(fileNames).concatMap { (name, fileStream) ->
                Mono.fromRunnable<Unit> {
                    zos.putNextEntry(ZipEntry(name.substringAfterLast("/")))
                }.thenMany(fileStream)
                    .doOnNext { dataBuffer ->
                        val bytes = ByteArray(dataBuffer.readableByteCount())
                        dataBuffer.read(bytes)
                        zos.write(bytes)
                        DataBufferUtils.release(dataBuffer) // Очищаем память
                    }
                    .doOnComplete { zos.closeEntry() }
            }
                .doOnTerminate {
                    zos.close()
                    emitter.complete()
                }
                .doOnError { emitter.error(it) }
                .subscribe()
        }
    }

    fun getPhotos(fileNames: List<String>): Flux<Pair<String, Flux<DataBuffer>>> {
        return Flux.fromIterable(fileNames).flatMap { fileName ->
            val cleanedName = fileName.removePrefix("/")

            val getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(cleanedName)
                .build()

            val fileStream: Flux<DataBuffer> = Mono.fromFuture(
                s3AsyncClient.getObject(getRequest, AsyncResponseTransformer.toPublisher())
            )
                .flatMapMany { responsePublisher ->
                    Flux.from(responsePublisher)
                }
                .map { byteBuffer ->
                    DefaultDataBufferFactory.sharedInstance.wrap(byteBuffer)
                }

            Mono.just(Pair(cleanedName, fileStream))
        }
    }
}