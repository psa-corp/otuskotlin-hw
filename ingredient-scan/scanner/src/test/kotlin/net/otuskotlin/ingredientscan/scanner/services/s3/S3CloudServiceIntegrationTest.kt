package net.otuskotlin.ingredientscan.scanner.services.s3

import io.awspring.cloud.s3.S3Template
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.multipart.MultipartFile
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.services.s3.S3Client

@Testcontainers
@SpringBootTest(properties = ["spring.cloud.aws.s3.bucket.name=test-bucket"])
@ActiveProfiles("test")
internal class S3CloudServiceIntegrationTest {

    companion object {
        @Container
        val minio = MinIOContainer(DockerImageName.parse("minio/minio:latest"))

        @DynamicPropertySource
        @JvmStatic
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.aws.s3.endpoint") {
                "http://${minio.host}:${minio.firstMappedPort}"
            }
            registry.add("spring.cloud.aws.credentials.access-key") { minio.userName }
            registry.add("spring.cloud.aws.credentials.secret-key") { minio.password   }
            registry.add("spring.cloud.aws.region.static") { "us-east-1" }
            registry.add("spring.cloud.aws.s3.path-style-access-enabled") { "true" }
        }
    }

    @Autowired
    private lateinit var s3Service: S3CloudService

    @Autowired
    private lateinit var s3Client: S3Client

    @Autowired
    private lateinit var s3Template: S3Template

    private lateinit var context: IsContext

    @BeforeEach
    fun setUp() {
        context = IsContext()
    }

    @Test
    fun `uploadFile success`() {
        // Arrange
        val mockFile = MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".toByteArray())

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, null)

        // Assert
        Assertions.assertThat(fileName).isNotNull
        Assertions.assertThat(context.errors).isEmpty()
        Assertions.assertThat(s3Service.fileExists(fileName!!)).isTrue
    }

    @Test
    fun `uploadFile with prefix`() {
        // Arrange
        val mockFile = MockMultipartFile("photo.jpg", "photo.jpg", "image/jpeg", "data".toByteArray())

        // Act
        val fileName = s3Service.uploadFile(context, mockFile, "photos")

        // Assert
        Assertions.assertThat(fileName).isNotNull
        Assertions.assertThat(fileName).contains("photos/")
        Assertions.assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFile success multiple times`() {
        // Arrange
        val mockFile = MockMultipartFile("photo.jpg", "photo.jpg", "image/jpeg", "content".toByteArray())

        // Act - upload same file multiple times (different UUIDs)
        val fileName1 = s3Service.uploadFile(context, mockFile, null)
        val fileName2 = s3Service.uploadFile(context, mockFile, null)

        // Assert - both should succeed with different names
        assertThat(fileName1).isNotNull
        assertThat(fileName2).isNotNull
        assertThat(fileName1).isNotEqualTo(fileName2)  // Different UUIDs
        assertThat(context.errors).isEmpty()
    }

    @Test
    fun `uploadFiles success`() {
        // Arrange
        val files: Array<MultipartFile> = arrayOf(
            MockMultipartFile("1.jpg", "1.jpg", "image/jpeg", "data1".toByteArray()),
            MockMultipartFile("2.jpg", "2.jpg", "image/jpeg", "data2".toByteArray())
        )

        // Act
        val names = s3Service.uploadFiles(context, files, null)

        // Assert
        Assertions.assertThat(names).hasSize(2)
        Assertions.assertThat(context.errors).isEmpty()
        names.forEach { name ->
            Assertions.assertThat(s3Service.fileExists(name)).isTrue
        }
    }

    @Test
    fun `uploadFiles no files error`() {
        // Arrange
        val emptyFiles = arrayOf<MultipartFile>()

        // Act
        val names = s3Service.uploadFiles(context, emptyFiles, null)

        // Assert
        Assertions.assertThat(names).isEmpty()
        Assertions.assertThat(context.errors).isNotEmpty
        Assertions.assertThat(context.errors.first().code).isEqualTo("NO_FILES")
    }

    @Test
    fun `uploadFiles too many`() {
        // Arrange
        val tooManyFiles: Array<MultipartFile> = Array(6) {
            MockMultipartFile("file$it.jpg", "file$it.jpg", "image/jpeg", "data$it".toByteArray())
        }

        // Act
        val names = s3Service.uploadFiles(context, tooManyFiles, null)

        // Assert
        Assertions.assertThat(names).isEmpty()
        Assertions.assertThat(context.errors).isNotEmpty
        Assertions.assertThat(context.errors.first().code).isEqualTo("TOO_MANY_FILES")
    }

    @Test
    fun `downloadFileAsResource success`() {
        // Arrange
        val mockFile = MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".toByteArray())
        val fileName = s3Service.uploadFile(context, mockFile, null)!!

        // Act
        val resource = s3Service.downloadFileAsResource(context, fileName)

        // Assert
        Assertions.assertThat(resource).isNotNull
        Assertions.assertThat(context.errors).isEmpty()
    }

    @Test
    fun `downloadFileAsResource not found`() {
        // Act
        val resource = s3Service.downloadFileAsResource(context, "nonexistent.jpg")

        // Assert
        Assertions.assertThat(resource).isNull()
        Assertions.assertThat(context.errors).isNotEmpty
        Assertions.assertThat(context.errors.first().code).isEqualTo("FILE_NOT_FOUND")
    }

    @Test
    fun `deleteFile success`() {
        // Arrange
        val mockFile = MockMultipartFile("delete.jpg", "delete.jpg", "image/jpeg", "content".toByteArray())
        val fileName = s3Service.uploadFile(context, mockFile, null)!!
        Assertions.assertThat(s3Service.fileExists(fileName)).isTrue

        // Act
        val deleted = s3Service.deleteFile(context, fileName)

        // Assert
        Assertions.assertThat(deleted).isTrue
        Assertions.assertThat(context.errors).isEmpty()
        Assertions.assertThat(s3Service.fileExists(fileName)).isFalse
    }

    @Test
    fun `deleteFile not found`() {
        // Act
        val deleted = s3Service.deleteFile(context, "missing.jpg")

        // Assert
        Assertions.assertThat(deleted).isFalse
        Assertions.assertThat(context.errors).isNotEmpty
        Assertions.assertThat(context.errors.first().code).isEqualTo("FILE_NOT_FOUND")
    }
}