plugins {
    id("plugin-jvm")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "net.otuskotlin.ingredientscan.tests"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(kotlin("stdlib"))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.docker.compose)

    testImplementation("net.otuskotlin.ingredientscan:core-common")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-jackson")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-mappers")

    testImplementation(libs.okhttp)

    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.kotlin)
    testImplementation(libs.jackson.datatype)

    testImplementation(libs.logback.classic)

    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk)


    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)

    testImplementation(libs.spring.boot.testcontainers)

    testImplementation(libs.logback.access.tomcat)
    testImplementation(libs.kafka.clients)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}


tasks {
    bootJar {
        enabled = false
    }

    build {
        dependsOn("testAll")
    }

    // Задача для запуска Docker Compose перед тестами
    val dockerComposeUp by registering(Exec::class) {
        group = "verification"
        description = "Запускает Docker Compose для тестов"

        val composeFile = file("docker-compose/docker-compose-test.yml")

        println("🔍 Проверяем Docker Compose файл: ${composeFile.absolutePath}")
        println("📁 Файл существует? ${composeFile.exists()}")

        if (!composeFile.exists()) {
            throw GradleException("❌ Docker Compose file not found: ${composeFile.absolutePath}")
        }

        println("✅ Docker Compose файл найден!")

        // Используем правильную команду
        commandLine = listOf(
            "docker", "compose",
            "-f", composeFile.absolutePath,
            "up", "-d"
        )

        doFirst {
            println("🚀 ЗАПУСКАЕМ DOCKER COMPOSE...")
            println("   Команда: docker compose -f ${composeFile.absolutePath} up -d")
        }

        doLast {
            println("✅ Docker Compose команда выполнена")
            println("⏳ Ожидаем запуска сервисов...")

            var attempts = 0
            val maxAttempts = 60 // 2 минуты
            var isHealthy = false

            while (attempts < maxAttempts) {
                try {
                    // Проверяем здоровье приложения
                    val healthUrl = "http://localhost:8081/v1/actuator/health"
                    val process = ProcessBuilder("curl", "-f", healthUrl)
                        .redirectErrorStream(true)
                        .start()

                    if (process.waitFor() == 0) {
                        println("✅ Все сервисы запущены и готовы!")
                        isHealthy = true
                        return@doLast
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки
                }

                attempts++
                Thread.sleep(2000)

                if (attempts % 10 == 0) {
                    println("   ⏳ Ожидание... ($attempts/$maxAttempts)")
                }
            }

            if (!isHealthy) {
                println("❌ Не удалось дождаться здоровья приложения")
                println("   Проверяем контейнеры...")
                try {
                    val psProcess = ProcessBuilder("docker", "ps")
                        .redirectErrorStream(true)
                        .start()
                    psProcess.inputStream.bufferedReader().forEachLine {
                        println("   $it")
                    }
                } catch (e: Exception) {
                    println("   Не удалось проверить контейнеры")
                }
            }
        }
    }

    // Задача для остановки Docker Compose после тестов
    val dockerComposeDown by registering(Exec::class) {
        group = "verification"
        description = "Останавливает Docker Compose после тестов"

        val composeFile = file("docker-compose/docker-compose-test.yml")
        commandLine = listOf(
            "docker", "compose",
            "-f", composeFile.absolutePath,
            "down", "-v"
        )

        doFirst {
            println("🛑 Останавливаем Docker Compose...")
        }

        doLast {
            println("✅ Docker Compose остановлен")
        }
    }

    // ОБЩАЯ задача для всех тестов
    val testAll by registering(Test::class) {
        group = "verification"
        description = "Запускает ВСЕ тесты с предварительным запуском Docker Compose"

        dependsOn(dockerComposeUp)
        finalizedBy(dockerComposeDown)

        configureByteBuddyAgent(this)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    // Задача ТОЛЬКО для REST тестов
    register<Test>("testRest") {
        group = "verification"
        description = "Runs REST integration tests with Docker Compose"

        dependsOn(dockerComposeUp)
        finalizedBy(dockerComposeDown)

        filter {
            includeTestsMatching("*Rest*")
        }

        systemProperty("spring.docker.compose.enabled", "false")
        configureByteBuddyAgent(this)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    // Задача ТОЛЬКО для Kafka тестов
    register<Test>("testKafka") {
        group = "verification"
        description = "Runs Kafka integration tests with Docker Compose"

        dependsOn(dockerComposeUp)
        finalizedBy(dockerComposeDown)

        filter {
            includeTestsMatching("*Kafka*")
        }

        systemProperty("spring.docker.compose.enabled", "false")
        configureByteBuddyAgent(this)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    // Общая конфигурация для всех тестовых задач
    withType<Test>().configureEach {
        // Отключаем Spring Boot Docker Compose Support
        systemProperty("spring.docker.compose.enabled", "false")
        systemProperty("spring.docker.compose.skip.in-tests", "true")
        workingDir = projectDir

        configureByteBuddyAgent(this)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

// Функция для настройки ByteBuddy agent
fun configureByteBuddyAgent(testTask: Test) {
    val byteBuddyAgent = testTask.project.configurations.testRuntimeClasspath.get()
        .files
        .find { it.name.startsWith("byte-buddy-agent-") }

    if (byteBuddyAgent != null) {
        val agentPath = byteBuddyAgent.absolutePath
            .replace("\\", "/") // Важно для Windows

        testTask.jvmArgs = listOf("-javaagent:$agentPath")
    } else {
        testTask.project.logger.warn("ByteBuddy agent JAR not found in test runtime classpath.")
    }

    testTask.jvmArgs?.addAll(listOf(
        "-XX:+EnableDynamicAgentLoading",
        "-Djdk.instrument.traceUsage=false"
    ))
}
