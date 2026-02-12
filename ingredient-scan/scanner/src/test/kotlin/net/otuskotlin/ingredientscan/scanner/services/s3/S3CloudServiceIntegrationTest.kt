//package net.otuskotlin.ingredientscan.scanner.services.s3
//
//import net.otuskotlin.ingredientscan.core.common.external.IsContext
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.ArgumentMatchers.any
//import org.mockito.ArgumentMatchers.anyString
//import org.mockito.ArgumentMatchers.eq
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.Mockito.mock
//import org.mockito.junit.jupiter.MockitoExtension
//import org.mockito.junit.jupiter.MockitoSettings
//import org.mockito.quality.Strictness
//import org.springframework.core.io.buffer.DefaultDataBufferFactory
//import org.springframework.http.HttpHeaders
//import org.springframework.http.codec.multipart.FilePart
//import reactor.core.publisher.Flux
//import reactor.test.StepVerifier
//import software.amazon.awssdk.core.async.AsyncRequestBody
//import software.amazon.awssdk.services.s3.S3AsyncClient
//import software.amazon.awssdk.services.s3.S3Client
//import software.amazon.awssdk.services.s3.model.*
//import java.util.concurrent.CompletableFuture
//
//@ExtendWith(MockitoExtension::class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class S3CloudServiceUnitTest {
//
//    @Mock
//    private lateinit var s3Template: io.awspring.cloud.s3.S3Template
//
//    @Mock
//    private lateinit var s3AsyncClient: S3AsyncClient
//
//    @Mock
//    private lateinit var s3Client: S3Client
//
//    private lateinit var s3Service: S3CloudService
//    private lateinit var context: IsContext
//
//    private val bucketName = "test-bucket"
//    private val maxFiles = 3
//
//    @BeforeEach
//    fun setUp() {
//        s3Service = S3CloudService(
//            s3Template,
//            s3AsyncClient,
//            s3Client,
//            bucketName,
//            maxFiles
//        )
//        context = IsContext()
//    }
//
//    @Test
//    fun `uploadFiles successfully uploads multiple files`() {
//        // Arrange
//        val files = Flux.just(
//            mockFilePart("1.jpg"),
//            mockFilePart("2.jpg")
//        )
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(false)
//
//        mockSuccessfulPut()
//
//        StepVerifier.create(
//            s3Service.uploadFiles(context, files, null)
//        )
//            .assertNext { names ->
//                assertThat(names).hasSize(2)
//                assertThat(names[0]).contains(".jpg")
//                assertThat(names[1]).contains(".jpg")
//            }
//            .verifyComplete()
//
//        assertThat(context.errors).isEmpty()
//    }
//
//    @Test
//    fun `uploadFiles returns error when too many files`() {
//        // Arrange
//        val files = Flux.range(0, 5)
//            .map { mockFilePart("file$it.jpg") }
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(false)
//
//        `when`(
//            s3AsyncClient.putObject(
//                any(PutObjectRequest::class.java),
//                any(AsyncRequestBody::class.java)
//            )
//        ).thenReturn(
//            CompletableFuture.completedFuture(
//                PutObjectResponse.builder().build()
//            )
//        )
//
//        StepVerifier.create(
//            s3Service.uploadFiles(context, files, null)
//        )
//            .expectError(IllegalArgumentException::class.java)
//            .verify()
//
//        assertThat(context.errors).hasSize(1)
//        assertThat(context.errors.first().code).isEqualTo("TOO_MANY_FILES")
//    }
//
//    @Test
//    fun `uploadFiles returns error when no files`() {
//        // Arrange
//        StepVerifier.create(
//            s3Service.uploadFiles(context, Flux.empty(), null)
//        )
//            .expectError(IllegalArgumentException::class.java)
//            .verify()
//
//        assertThat(context.errors).hasSize(1)
//        assertThat(context.errors.first().code).isEqualTo("NO_FILES")
//    }
//
//    @Test
//    fun `uploadFile successfully uploads single file`() {
//        // Arrange
//        val file = mockFilePart("photo.jpg")
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(false)
//
//        mockSuccessfulPut()
//
//        StepVerifier.create(
//            s3Service.uploadFile(context, file, null)
//        )
//            .assertNext { fileName ->
//                assertThat(fileName).contains("photo.jpg")
//            }
//            .verifyComplete()
//
//        assertThat(context.errors).isEmpty()
//    }
//
//    @Test
//    fun `uploadFile returns error when file already exists`() {
//        // Arrange
//        val file = mockFilePart("photo.jpg")
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(true)
//
//        StepVerifier.create(
//            s3Service.uploadFile(context, file, null)
//        )
//            .expectError(IllegalStateException::class.java)
//            .verify()
//
//        assertThat(context.errors).hasSize(1)
//        assertThat(context.errors.first().code).isEqualTo("FILE_EXISTS")
//    }
//
//    @Test
//    fun `uploadFile returns error when s3 fails`() {
//        // Arrange
//        val file = mockFilePart("photo.jpg")
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(false)
//
//        `when`(
//            s3AsyncClient.putObject(
//                any(PutObjectRequest::class.java),
//                any(AsyncRequestBody::class.java)
//            )
//        ).thenReturn(
//            CompletableFuture.failedFuture(RuntimeException("S3 down"))
//        )
//
//        StepVerifier.create(
//            s3Service.uploadFile(context, file, null)
//        )
//            .expectError(RuntimeException::class.java)
//            .verify()
//
//        assertThat(context.errors).hasSize(1)
//        assertThat(context.errors.first().code).isEqualTo("STORE_NOT_FOUND")
//    }
//
//    @Test
//    fun `uploadFile with prefix adds prefix to filename`() {
//        // Arrange
//        val file = mockFilePart("photo.jpg")
//
//        `when`(s3Template.objectExists(eq(bucketName), anyString()))
//            .thenReturn(false)
//
//        mockSuccessfulPut()
//
//        StepVerifier.create(
//            s3Service.uploadFile(context, file, "photos")
//        )
//            .assertNext { fileName ->
//                assertThat(fileName).startsWith("photos/")
//            }
//            .verifyComplete()
//
//        assertThat(context.errors).isEmpty()
//    }
//
//    private fun mockFilePart(filename: String, content: ByteArray = "data".toByteArray()): FilePart {
//        val bufferFactory = DefaultDataBufferFactory()
//        val dataBuffer = bufferFactory.wrap(content)
//
//        val filePart = mock(FilePart::class.java)
//        `when`(filePart.filename()).thenReturn(filename)
//        `when`(filePart.content()).thenReturn(Flux.just(dataBuffer))
//        `when`(filePart.headers()).thenReturn(HttpHeaders())
//
//        return filePart
//    }
//
//    private fun mockSuccessfulPut() {
//        `when`(
//            s3AsyncClient.putObject(
//                any(PutObjectRequest::class.java),
//                any(AsyncRequestBody::class.java)
//            )
//        ).thenReturn(
//            CompletableFuture.completedFuture(
//                PutObjectResponse.builder().build()
//            )
//        )
//    }
//}