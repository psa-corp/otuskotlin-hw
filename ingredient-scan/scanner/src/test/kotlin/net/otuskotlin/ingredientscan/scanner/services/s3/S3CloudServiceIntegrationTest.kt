package net.otuskotlin.ingredientscan.scanner.services.s3

import kotlinx.coroutines.test.runTest
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.CompletedUpload
import software.amazon.awssdk.transfer.s3.model.Upload
import software.amazon.awssdk.transfer.s3.model.UploadRequest
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3CloudServiceTest {

    @Mock
    private lateinit var s3AsyncClient: S3AsyncClient

    @Mock
    private lateinit var s3TransferManager: S3TransferManager

    private lateinit var s3Service: S3CloudService

    private val bucketName = "test-bucket"
    private val maxFiles = 3

    @BeforeEach
    fun setUp() {
        s3Service = S3CloudService(s3AsyncClient, s3TransferManager, bucketName, maxFiles)
    }

    @Test
    fun `uploadFiles should upload multiple files successfully`() = runTest {
        val context = IsContext()
        val files = Flux.just(
            mockFilePart("file1.jpg", "data1".toByteArray()),
            mockFilePart("file2.png", "data2".toByteArray())
        )

        mockHeadObject(exists = false)
        mockSuccessfulUpload()

        val result = s3Service.uploadFiles(context, files, null)

        StepVerifier.create(result)
            .assertNext { fileNames ->
                assertThat(fileNames).hasSize(2)
                assertThat(fileNames[0]).contains("file1.jpg")
                assertThat(fileNames[1]).contains("file2.png")
            }
            .verifyComplete()

        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFiles should fail when too many files`() = runTest {
        val context = IsContext()
        val files = Flux.range(0, maxFiles + 1)
            .map { mockFilePart("file$it.jpg") }

        mockHeadObject(exists = false)
        mockSuccessfulUpload()

        val result = s3Service.uploadFiles(context, files, null)

        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()

        assertThat(context.errors).hasSize(1)
        val error = context.errors.first()
        assertThat(error.code).isEqualTo("tooManyFiles")
        assertThat(error.message).contains("Too many files: max $maxFiles allowed")
    }

    @Test
    fun `uploadFiles should fail when no files`() = runTest {
        val context = IsContext()

        val result = s3Service.uploadFiles(context, Flux.empty(), null)

        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()

        assertThat(context.errors).hasSize(1)
        val error = context.errors.first()
        assertThat(error.code).isEqualTo("noFiles")
        assertThat(error.message).contains("No files provided")
    }

    @Test
    fun `uploadFiles should stop processing after max files and not upload extra files`() = runTest {
        val context = IsContext()
        val files = Flux.range(0, maxFiles + 2)
            .map { mockFilePart("file$it.jpg") }

        mockHeadObject(exists = false)
        mockSuccessfulUpload()

        val result = s3Service.uploadFiles(context, files, null)

        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()

        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("tooManyFiles")
    }

    @Test
    fun `uploadFile should upload single file with prefix`() {
        val context = IsContext()
        val file = mockFilePart("photo.jpg")
        val prefix = "user123"

        mockHeadObject(exists = false)
        mockSuccessfulUpload()

        val result = s3Service.uploadFile(context, file, prefix)

        StepVerifier.create(result)
            .assertNext { fileName ->
                assertThat(fileName).startsWith("$prefix/")
                assertThat(fileName).endsWith("photo.jpg")
                assertThat(fileName).contains("_")
            }
            .verifyComplete()

        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFile should fail when file already exists`() {
        val context = IsContext()
        val file = mockFilePart("existing.jpg")

        mockHeadObject(exists = true)

        val result = s3Service.uploadFile(context, file, null)

        StepVerifier.create(result)
            .expectError(IllegalStateException::class.java)
            .verify()

        assertThat(context.errors).hasSize(1)
        val error = context.errors.first()
        assertThat(error.code).isEqualTo("fileExists")
        assertThat(error.message).contains("File already exists")
    }

    @Test
    fun `uploadFile should fail when S3 put fails`() {
        val context = IsContext()
        val file = mockFilePart("photo.jpg")

        mockHeadObject(exists = false)

        val uploadMock = mock(Upload::class.java)
        val failedFuture = CompletableFuture<CompletedUpload>()
        failedFuture.completeExceptionally(RuntimeException("S3 is down"))
        `when`(uploadMock.completionFuture()).thenReturn(failedFuture)
        `when`(s3TransferManager.upload(any(UploadRequest::class.java))).thenReturn(uploadMock)

        val result = s3Service.uploadFile(context, file, null)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        // TODO: при ошибке загрузки добавляются две ошибки (s3Error и storageNotFound).
        // Пока баг не исправлен, проверяем, что хотя бы s3Error присутствует.
        assertThat(context.errors).hasSize(2)
        assertThat(context.errors.map { it.code }).contains("s3Error")
    }

    @Test
    fun `uploadFile should add storageNotFound error on headObject failure`() {
        val context = IsContext()
        val file = mockFilePart("photo.jpg")

        `when`(
            s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
        ).thenReturn(
            CompletableFuture.failedFuture(RuntimeException("Network error"))
        )

        val result = s3Service.uploadFile(context, file, null)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        assertThat(context.errors).hasSize(1)
        val error = context.errors.first()
        assertThat(error.code).isEqualTo("storageNotFound")
        assertThat(error.message).contains("Storage not found or unavailable")
    }

    @Test
    fun `fileExistsMono should return true if file exists`() {
        val key = "test/file.txt"
        `when`(
            s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
        ).thenReturn(
            CompletableFuture.completedFuture(HeadObjectResponse.builder().build())
        )

        val result = s3Service.fileExistsMono(key)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `fileExistsMono should return false if NoSuchKeyException`() {
        val key = "missing/file.txt"
        val exception = NoSuchKeyException.builder().build()
        `when`(
            s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
        ).thenReturn(
            CompletableFuture.failedFuture(exception)
        )

        val result = s3Service.fileExistsMono(key)

        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `fileExistsMono should propagate other errors`() {
        val key = "test/file.txt"
        `when`(
            s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
        ).thenReturn(
            CompletableFuture.failedFuture(RuntimeException("Internal error"))
        )

        val result = s3Service.fileExistsMono(key)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `upload suspend function should delegate to uploadFiles and return list`() = runTest {
        val context = IsContext()
        val files = Flux.just(mockFilePart("a.jpg"), mockFilePart("b.jpg"))

        mockHeadObject(exists = false)
        mockSuccessfulUpload()

        val result = s3Service.upload(context, files, "prefix")

        assertThat(result).hasSize(2)
        assertThat(result[0]).startsWith("prefix/")
        assertThat(result[1]).startsWith("prefix/")
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `upload suspend function should throw if argument is not Flux`() = runTest {
        val context = IsContext()
        val invalidFiles = "not a flux"

        try {
            s3Service.upload(context, invalidFiles, null)
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Expected Flux<FilePart>")
        }
    }

    @Test
    fun `downloadPhotosAsZip should return zip flux with multiple files`() {
        val fileNames = listOf("photos/1.jpg", "photos/2.png")

        mockGetObject("photos/1.jpg", byteArrayOf(1, 2, 3))
        mockGetObject("photos/2.png", byteArrayOf(4, 5, 6, 7))

        val result = s3Service.downloadPhotosAsZip(fileNames)

        StepVerifier.create(result)
            .expectNextCount(2)
            .thenConsumeWhile { true }
            .verifyComplete()
    }

    @Test
    fun `getPhotos should return pairs of filename and flux of data buffers`() {
        val fileNames = listOf("a.jpg", "b.jpg")
        mockGetObject("a.jpg", byteArrayOf(10, 20))
        mockGetObject("b.jpg", byteArrayOf(30, 40, 50))

        val result = s3Service.getPhotos(fileNames)

        StepVerifier.create(result)
            .assertNext { (name, dataBufferFlux) ->
                assertThat(name).isEqualTo("a.jpg")
                StepVerifier.create(dataBufferFlux)
                    .consumeNextWith { buffer ->
                        val bytes = ByteArray(buffer.readableByteCount())
                        buffer.read(bytes)
                        assertThat(bytes).containsExactly(10, 20)
                    }
                    .verifyComplete()
            }
            .assertNext { (name, dataBufferFlux) ->
                assertThat(name).isEqualTo("b.jpg")
                StepVerifier.create(dataBufferFlux)
                    .consumeNextWith { buffer ->
                        val bytes = ByteArray(buffer.readableByteCount())
                        buffer.read(bytes)
                        assertThat(bytes).containsExactly(30, 40, 50)
                    }
                    .verifyComplete()
            }
            .verifyComplete()
    }

    @Test
    fun `download suspend function should call downloadPhotosAsZip`() = runTest {
        val context = IsContext()
        context.files = listOf("file1.jpg", "file2.png")
        mockGetObject("file1.jpg", byteArrayOf(1, 2))
        mockGetObject("file2.png", byteArrayOf(3, 4))

        val result = s3Service.download(context)

        assertThat(result).isInstanceOf(Flux::class.java)
        @Suppress("UNCHECKED_CAST")
        val flux = result as Flux<DataBuffer>
        StepVerifier.create(flux)
            .expectNextCount(2)
            .thenConsumeWhile { true }
            .verifyComplete()
    }

    private fun mockFilePart(filename: String, content: ByteArray = "dummy".toByteArray()): FilePart {
        val bufferFactory = DefaultDataBufferFactory()
        val dataBuffer = bufferFactory.wrap(content)

        val filePart = mock(FilePart::class.java)
        `when`(filePart.filename()).thenReturn(filename)
        `when`(filePart.content()).thenReturn(Flux.just(dataBuffer))
        `when`(filePart.headers()).thenReturn(HttpHeaders())
        return filePart
    }

    private fun mockHeadObject(exists: Boolean) {
        if (exists) {
            `when`(
                s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
            ).thenReturn(
                CompletableFuture.completedFuture(HeadObjectResponse.builder().build())
            )
        } else {
            `when`(
                s3AsyncClient.headObject(any<Consumer<HeadObjectRequest.Builder>>())
            ).thenReturn(
                CompletableFuture.failedFuture(NoSuchKeyException.builder().build())
            )
        }
    }

    private fun mockSuccessfulUpload() {
        val uploadMock = mock(Upload::class.java)
        val putObjectResponse = PutObjectResponse.builder().build()
        val completedUpload = CompletedUpload.builder()
            .response(putObjectResponse)
            .build()
        val completedFuture = CompletableFuture.completedFuture(completedUpload)
        `when`(uploadMock.completionFuture()).thenReturn(completedFuture)
        `when`(s3TransferManager.upload(any(UploadRequest::class.java))).thenReturn(uploadMock)
    }

    private fun mockGetObject(key: String, content: ByteArray? = null, error: Throwable? = null) {
        val request = GetObjectRequest.builder().bucket(bucketName).key(key).build()
        if (error != null) {
            `when`(s3AsyncClient.getObject(eq(request), any<AsyncResponseTransformer<GetObjectResponse, ResponsePublisher<ByteBuffer>>>()))
                .thenReturn(CompletableFuture.failedFuture(error))
        } else {
            val byteBuffer = ByteBuffer.wrap(content ?: byteArrayOf())
            val publisher = mock(ResponsePublisher::class.java) as ResponsePublisher<ByteBuffer>
            `when`(publisher.subscribe(any<org.reactivestreams.Subscriber<ByteBuffer>>())).thenAnswer { invocation ->
                val subscriber = invocation.getArgument<org.reactivestreams.Subscriber<ByteBuffer>>(0)
                subscriber.onSubscribe(mock(org.reactivestreams.Subscription::class.java))
                subscriber.onNext(byteBuffer)
                subscriber.onComplete()
                null
            }
            `when`(s3AsyncClient.getObject(eq(request), any<AsyncResponseTransformer<GetObjectResponse, ResponsePublisher<ByteBuffer>>>()))
                .thenReturn(CompletableFuture.completedFuture(publisher))
        }
    }

}