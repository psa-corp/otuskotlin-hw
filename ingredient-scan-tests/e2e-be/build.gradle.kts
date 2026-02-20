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
    testImplementation(libs.spring.boot.starter.webflux)

    testImplementation("net.otuskotlin.ingredientscan:core-common")
    testImplementation("net.otuskotlin.ingredientscan:core-stubs")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-jackson")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-mappers")

    testImplementation(libs.okhttp)

    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.kotlin)
    testImplementation(libs.jackson.datatype)

    testImplementation(libs.logback.classic)

    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk)

    testImplementation(libs.kafka.clients)


    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)

    testImplementation(libs.software.amazon)
    testImplementation(libs.software.amazon.client)
    testImplementation(libs.coroutines.core)

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

    test {
        // Отключаем встроенный Docker Compose поддержку Spring Boot
        systemProperty("spring.docker.compose.enabled", "false")
        systemProperty("spring.docker.compose.skip.in-tests", "true")

        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        jvmArgs = listOf(
            "-XX:+EnableDynamicAgentLoading",
            "-Djdk.instrument.traceUsage=false"
        )
    }
}