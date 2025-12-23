package net.otuskotlin.ingredientscan.scanner.services.s3

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Resource
import io.awspring.cloud.s3.S3Template
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.IOException

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3CloudServiceUnitTest {

    @Mock
    private lateinit var s3Template: S3Template

    @Mock
    private lateinit var s3Client: S3Client

    private lateinit var s3Service: S3CloudService
    private lateinit var context: IsContext

    private val bucketName = "test-bucket"
    private val maxFiles = 3

    @BeforeEach
    fun setUp() {
        s3Service = S3CloudService(s3Template, s3Client, bucketName, maxFiles)
        context = IsContext()
    }

    @Test
    fun `uploadFile successfully uploads file and returns filename`() {
        // Arrange
        val mockFile = MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".toByteArray())

        `when`(s3Template.objectExists(eq(bucketName), anyString())).thenReturn(false)
        `when`(s3Template.upload(eq(bucketName), anyString(), any(), any(ObjectMetadata::class.java)))
            .thenReturn(mock(S3Resource::class.java))

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, null)

        // Assert
        assertThat(fileName).isNotNull
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFile returns null and error when file already exists`() {
        // Arrange
        val mockFile = MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".toByteArray())

        `when`(s3Template.objectExists(eq(bucketName), anyString())).thenReturn(true)

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, null)

        // Assert
        assertThat(fileName).isNull()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("FILE_EXISTS")
    }

    @Test
    fun `uploadFiles successfully uploads multiple files`() {
        // Arrange
        val files: Array<MultipartFile> = arrayOf(
            MockMultipartFile("1.jpg", "1.jpg", "image/jpeg", "data1".toByteArray()),
            MockMultipartFile("2.jpg", "2.jpg", "image/jpeg", "data2".toByteArray())
        )

        `when`(s3Template.objectExists(eq(bucketName), anyString())).thenReturn(false)
        `when`(s3Template.upload(eq(bucketName), anyString(), any(), any(ObjectMetadata::class.java)))
            .thenReturn(mock(S3Resource::class.java))

        // Act
        val names = s3Service.uploadFiles(context, files, null)

        // Assert
        assertThat(names).hasSize(2)
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFiles returns empty list and error when too many files`() {
        // Arrange
        val tooManyFiles: Array<MultipartFile> = Array(6) {
            MockMultipartFile("file$it.jpg", "file$it.jpg", "image/jpeg", "data$it".toByteArray())
        }

        // Act
        val names = s3Service.uploadFiles(context, tooManyFiles, null)

        // Assert
        assertThat(names).isEmpty()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("TOO_MANY_FILES")
    }

    @Test
    fun `uploadFiles returns empty list and error when no files`() {
        // Arrange
        val emptyFiles = arrayOf<MultipartFile>()

        // Act
        val names = s3Service.uploadFiles(context, emptyFiles, null)

        // Assert
        assertThat(names).isEmpty()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("NO_FILES")
    }

    @Test
    fun `fileExists returns true when file exists in storage`() {
        // Arrange
        val fileName = "test.jpg"
        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(true)

        // Act
        val exists = s3Service.fileExists(fileName)

        // Assert
        assertThat(exists).isTrue()
    }

    @Test
    fun `fileExists returns false when file does not exist in storage`() {
        // Arrange
        val fileName = "nonexistent.jpg"
        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(false)

        // Act
        val exists = s3Service.fileExists(fileName)

        // Assert
        assertThat(exists).isFalse()
    }

    @Test
    fun `downloadFileAsResource returns resource when file exists`() {
        // Arrange
        val fileName = "test.jpg"
        val mockS3Resource = mock(S3Resource::class.java)

        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(true)
        `when`(s3Template.download(eq(bucketName), eq(fileName))).thenReturn(mockS3Resource)

        // Act
        val resource = s3Service.downloadFileAsResource(context, fileName)

        // Assert
        assertThat(resource).isNotNull
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `downloadFileAsResource returns null and error when file not found`() {
        // Arrange
        val fileName = "nonexistent.jpg"
        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(false)

        // Act
        val resource = s3Service.downloadFileAsResource(context, fileName)

        // Assert
        assertThat(resource).isNull()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("FILE_NOT_FOUND")
    }

    @Test
    fun `deleteFile returns true when file successfully deleted`() {
        // Arrange
        val fileName = "delete.jpg"

        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(true)
        `when`(s3Client.deleteObject(any(DeleteObjectRequest::class.java)))
            .thenReturn(DeleteObjectResponse.builder().build())

        // Act
        val deleted = s3Service.deleteFile(context, fileName)

        // Assert
        assertThat(deleted).isTrue()
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `deleteFile returns false and error when file not found`() {
        // Arrange
        val fileName = "missing.jpg"
        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(false)

        // Act
        val deleted = s3Service.deleteFile(context, fileName)

        // Assert
        assertThat(deleted).isFalse()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("FILE_NOT_FOUND")
    }

    @Test
    fun `deleteFile returns false and error when storage error occurs`() {
        // Arrange
        val fileName = "test.jpg"

        `when`(s3Template.objectExists(eq(bucketName), eq(fileName))).thenReturn(true)
        `when`(s3Client.deleteObject(any(DeleteObjectRequest::class.java)))
            .thenThrow(RuntimeException("Storage error"))

        // Act
        val deleted = s3Service.deleteFile(context, fileName)

        // Assert
        assertThat(deleted).isFalse()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("STORE_NOT_FOUND")
    }

    @Test
    fun `uploadFile with prefix returns filename containing prefix`() {
        // Arrange
        val mockFile = MockMultipartFile("photo.jpg", "photo.jpg", "image/jpeg", "data".toByteArray())

        `when`(s3Template.objectExists(eq(bucketName), anyString())).thenReturn(false)
        `when`(s3Template.upload(eq(bucketName), anyString(), any(), any(ObjectMetadata::class.java)))
            .thenReturn(mock(S3Resource::class.java))

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, "photos")

        // Assert
        assertThat(fileName).isNotNull
        assertThat(fileName).contains("photos/")
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFile returns null and error when storage error occurs`() {
        // Arrange
        val mockFile = MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".toByteArray())

        `when`(s3Template.objectExists(eq(bucketName), anyString())).thenReturn(false)
        doAnswer { _ ->
            throw IOException("Storage error")
        }.`when`(s3Template).upload(
            eq(bucketName),
            anyString(),
            any(),
            any(ObjectMetadata::class.java)
        )

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, null)

        // Assert
        assertThat(fileName).isNull()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("STORE_NOT_FOUND")
    }

    @Test
    fun `getObjectMetadata returns metadata when file exists`() {
        // Arrange
        val fileName = "test.jpg"
        val expectedMetadata = HeadObjectResponse.builder()
            .contentLength(1024L)
            .contentType("image/jpeg")
            .build()

        `when`(s3Client.headObject(any(HeadObjectRequest::class.java))).thenReturn(expectedMetadata)

        // Act
        val metadata = s3Service.getObjectMetadata(context, fileName)

        // Assert
        assertThat(metadata).isNotNull
        assertThat(metadata?.contentLength()).isEqualTo(1024L)
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `getObjectMetadata returns null and error when storage error occurs`() {
        // Arrange
        val fileName = "test.jpg"

        `when`(s3Client.headObject(any(HeadObjectRequest::class.java))).thenThrow(
            S3Exception.builder()
                .message("Not found")
                .build()
        )

        // Act
        val metadata = s3Service.getObjectMetadata(context, fileName)

        // Assert
        assertThat(metadata).isNull()
        assertThat(context.errors).hasSize(1)
        assertThat(context.errors.first().code).isEqualTo("STORE_NOT_FOUND")
    }
}